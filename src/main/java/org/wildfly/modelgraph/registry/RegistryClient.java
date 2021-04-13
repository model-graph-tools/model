package org.wildfly.modelgraph.registry;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
@Path("/registry/model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RegistryClient {

    @POST
    Uni<Response> register(ModelService modelService);

    @DELETE
    @Path("/{version}")
    Uni<Response> unregister(@PathParam("version") String version);
}
