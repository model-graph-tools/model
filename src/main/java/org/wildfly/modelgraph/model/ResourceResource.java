package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.wildfly.modelgraph.model.Failure.throwNotFound;

@Path("/resources")
public class ResourceResource {

    @Inject
    ResourceRepository repository;

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Resource> query(@QueryParam("name") String name) {
        return repository.resources(name);
    }

    @GET
    @Path("/resource")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Resource> resource(
            @QueryParam("address") String address,
            @QueryParam("skip") @DefaultValue("") String skip) {
        return repository.resource(address, skip)
                .onItem().transformToUni(resource ->
                        skip(skip, "a") ? Uni.createFrom().item(resource) : repository.assignAttributes(resource))
                .onItem().transformToUni(resource ->
                        skip(skip, "o") ? Uni.createFrom().item(resource) : repository.assignOperations(resource,
                                skip(skip, "g")))
                .onItem().transformToUni(resource ->
                        skip(skip, "c") ? Uni.createFrom().item(resource) : repository.assignCapabilities(resource));
    }

    @GET
    @Path("/subtree")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Resource> subtree(@QueryParam("address") String address) {
        return repository.subtree(address)
                .ifNoItem().after(Duration.ofSeconds(2))
                .recoverWithItem(Collections.emptyList())
                .onItem().transformToUni(resources -> {
                    List<Uni<Resource>> childrenUnis = resources.stream()
                            .map(resource -> repository.assignChildren(resource))
                            .collect(toList());
                    return Uni.combine().all().unis(childrenUnis).combinedWith(result -> {
                        Resource resource = null;
                        List<Resource> resourcesWithChildren = result.stream()
                                .filter(o -> o instanceof Resource)
                                .map(o -> (Resource) o)
                                .collect(toList());
                        if (!resourcesWithChildren.isEmpty()) {
                            Iterator<Resource> iterator = resourcesWithChildren.iterator();
                            Iterator<Resource> childIterator = resourcesWithChildren.iterator();
                            childIterator.next();
                            while (iterator.hasNext() && childIterator.hasNext()) {
                                Resource parent = iterator.next();
                                Resource child = childIterator.next();
                                for (int i = 0; i < parent.children.size(); i++) {
                                    if (parent.children.get(i).id.equals(child.id)) {
                                        parent.children.set(i, child);
                                    }
                                }
                            }
                            resource = resourcesWithChildren.get(0);
                        } else {
                            throwNotFound(address);
                        }
                        return resource;
                    });
                });
    }

    @GET
    @Path("/children")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Resource> children(@QueryParam("address") String address) {
        return repository.children(address);
    }

    @GET
    @Path("/deprecated")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Resource> deprecated(@QueryParam("since") @DefaultValue("") String since) {
        return repository.deprecated(Version.from(since));
    }

    static boolean skip(String skip, String what) {
        return skip != null && skip.contains(what);
    }
}
