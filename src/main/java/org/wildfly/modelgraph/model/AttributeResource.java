package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/attributes")
public class AttributeResource {

    @Inject AttributeRepository repository;

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Attribute> query(@QueryParam("name") String name) {
        return repository.attributes(name);
    }


    @GET
    @Path("/deprecated")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Attribute> deprecated(@QueryParam("since") @DefaultValue("") String since) {
        return repository.deprecated(Version.from(since));
    }
}
