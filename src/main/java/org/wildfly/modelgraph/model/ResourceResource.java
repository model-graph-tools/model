package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.wildfly.modelgraph.model.Failure.throwNotFound;

@Path("/resources")
public class ResourceResource {

    private static final Logger log = Logger.getLogger(ResourceResource.class);

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
            @QueryParam("skip") @DefaultValue("") String skip,
            @Context HttpHeaders headers) {
        boolean d = false;
        List<String> header = headers.getRequestHeader("mgt-diff");
        if (!header.isEmpty()) {
            d = Boolean.parseBoolean(header.get(0));
        }
        final boolean diff = d;

        // TODO Switch serializer if diff == true
        return repository.resource(address, skip)
                .flatMap(resource -> skip(skip, "a")
                        ? Uni.createFrom().item(resource)
                        : repository.assignAttributes(resource))
                .flatMap(resource -> skip(skip, "o")
                        ? Uni.createFrom().item(resource)
                        : repository.assignOperations(resource, skip(skip, "g")))
                .flatMap(resource -> skip(skip, "c") ?
                        Uni.createFrom().item(resource)
                        : repository.assignCapabilities(resource))
                .map(resource -> {
                    if (diff) {
                        log.debugf("Make resource %s comparable", resource.address);
                        makeComparable(resource);
                    }
                    return resource;
                });
    }

    @GET
    @Path("/subtree")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Resource> subtree(@QueryParam("address") String address) {
        return repository.subtree(address)
                .ifNoItem().after(Duration.ofSeconds(2))
                .recoverWithItem(Collections.emptyList())
                .flatMap(resources -> {
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

    private void makeComparable(Resource resource) {
        if (resource != null) {
            // properties
            stripId(resource);
            stripDeprecation(resource.deprecation);
            resource.childDescriptions = null;

            // relations
            makeComparable(resource.parent);
            if (resource.children != null) {
                resource.children.sort(comparing(r -> r.address));
                for (Resource child : resource.children) {
                    makeComparable(child);
                }
            }
            if (resource.operations != null) {
                resource.operations.sort(comparing(o -> o.name));
                for (Operation operation : resource.operations) {
                    makeComparable(operation);
                }
            }
            if (resource.attributes != null) {
                resource.attributes.sort(comparing(a -> a.name));
                for (Attribute attribute : resource.attributes) {
                    makeComparable(attribute);
                }
            }
            if (resource.capabilities != null) {
                resource.capabilities.sort(comparing(c -> c.name));
                for (Capability capability : resource.capabilities) {
                    stripId(capability);
                }
            }
        }
    }

    private void makeComparable(Operation operation) {
        if (operation != null) {
            stripId(operation);
            stripDeprecation(operation.deprecation);
            if (operation.parameters != null) {
                operation.parameters.sort(comparing(p -> p.name));
                for (Parameter parameter : operation.parameters) {
                    makeComparable(parameter);
                }
            }
        }
    }

    private void makeComparable(Attribute attribute) {
        if (attribute != null) {
            stripId(attribute);
            stripDeprecation(attribute.deprecation);
            if (attribute.attributes != null) {
                attribute.attributes.sort(comparing(a -> a.name));
                for (Attribute nestedAttribute : attribute.attributes) {
                    makeComparable(nestedAttribute);
                }
            }
        }
    }

    private void makeComparable(Parameter parameter) {
        if (parameter != null) {
            stripId(parameter);
            stripDeprecation(parameter.deprecation);
            if (parameter.parameters != null) {
                parameter.parameters.sort(comparing(p -> p.name));
                for (Parameter nestedParameter : parameter.parameters) {
                    makeComparable(nestedParameter);
                }
            }
        }
    }

    private void stripDeprecation(Deprecation deprecation) {
        if (deprecation != null) {
            stripId(deprecation.since);
        }
    }

    private void stripId(Model model) {
        if (model != null) {
            model.id = null;
        }
    }
}
