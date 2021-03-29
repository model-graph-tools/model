package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/capabilities")
public class CapabilityResource {

    @Inject CapabilityRepository repository;

    @GET
    @Path("/query")
    public Multi<Capability> query(@QueryParam("name") String name) {
        return repository.capabilities(name);
    }
}
