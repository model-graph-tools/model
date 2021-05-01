package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

    @Inject
    VersionRepository repository;

    @GET
    public Multi<Version> versions() {
        return repository.versions();
    }
}
