package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/operations")
public class OperationResource {

    @Inject OperationRepository repository;

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Operation> query(@QueryParam("name") String name) {
        return repository.operations(name);
    }

    @GET
    @Path("/deprecated")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Operation> deprecated(@QueryParam("since") String since) {
        return repository.deprecated(Version.from(since));
    }
}
