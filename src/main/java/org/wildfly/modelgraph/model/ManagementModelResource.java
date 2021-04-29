package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/management-model")
public class ManagementModelResource {

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    OperationRepository operationRepository;

    @Inject
    CapabilityRepository capabilityRepository;

    @Inject
    AttributeRepository attributeRepository;

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Models> query(@QueryParam("name") String name) {
        return Uni.createFrom().item(new Models())
                .onItem()
                .transformToUni(models -> resourceRepository.resources(name).collect().asList()
                        .onItem()
                        .transformToUni(resources -> {
                            models.resources = resources;
                            return Uni.createFrom().item(models);
                        }))
                .onItem()
                .transformToUni(models -> operationRepository.operations(name).collect().asList()
                        .onItem()
                        .transformToUni(operations -> {
                            models.operations = operations;
                            return Uni.createFrom().item(models);
                        }))
                .onItem()
                .transformToUni(models -> capabilityRepository.capabilities(name).collect().asList()
                        .onItem()
                        .transformToUni(capabilities -> {
                            models.capabilities = capabilities;
                            return Uni.createFrom().item(models);
                        }))
                .onItem()
                .transformToUni(models -> attributeRepository.attributes(name).collect().asList()
                        .onItem()
                        .transformToUni(attributes -> {
                            models.attributes = attributes;
                            return Uni.createFrom().item(models);
                        }));
    }

    @GET
    @Path("/deprecated")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Models> deprecated(@QueryParam("since") @DefaultValue("") String since) {
        Version version = Version.from(since);
        return Uni.createFrom().item(new Models())
                .onItem()
                .transformToUni(models -> resourceRepository.deprecated(version).collect().asList()
                        .onItem()
                        .transformToUni(resources -> {
                            models.resources = resources;
                            return Uni.createFrom().item(models);
                        }))
                .onItem()
                .transformToUni(models -> operationRepository.deprecated(version).collect().asList()
                        .onItem()
                        .transformToUni(operations -> {
                            models.operations = operations;
                            return Uni.createFrom().item(models);
                        }))
                .onItem()
                .transformToUni(models -> attributeRepository.deprecated(version).collect().asList()
                        .onItem()
                        .transformToUni(attributes -> {
                            models.attributes = attributes;
                            return Uni.createFrom().item(models);
                        }));
    }
}