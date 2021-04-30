package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/capabilities")
@Produces(MediaType.APPLICATION_JSON)
public class CapabilityResource {

    @Inject
    CapabilityRepository repository;

    @GET
    @Path("/query")
    public Multi<Capability> query(@QueryParam("name") String name) {
        return repository.capabilities(name, false);
    }
}
