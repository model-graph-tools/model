package org.wildfly.modelgraph.registry;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
@Path("/registry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RegistryClient {

    @POST
    Uni<Response> register(Registration registration);

    @DELETE
    @Path("/{version}")
    Response unregister(@PathParam("version") String version);
}
