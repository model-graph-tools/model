package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/versions")
public class VersionResource {

    @Inject
    VersionRepository repository;

    @GET
    public Multi<Version> versions() {
        return repository.versions();
    }
}
