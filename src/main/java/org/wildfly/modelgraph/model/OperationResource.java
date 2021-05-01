package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/operations")
@Produces(MediaType.APPLICATION_JSON)
public class OperationResource {

    @Inject OperationRepository repository;

    @GET
    @Path("/query")
    public Multi<Operation> query(@QueryParam("name") String name) {
        return repository.operations(name);
    }

    @GET
    @Path("/deprecated")
    public Multi<Operation> deprecated(@QueryParam("since") @DefaultValue("") String since) {
        return repository.deprecated(Version.from(since));
    }
}
