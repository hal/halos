package org.wildfly.halos.proxy;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/v1/instance")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InstanceResource {

    @Inject Dispatcher dispatcher;

    @GET
    public Iterable<Instance> list() {
        return dispatcher.instances();
    }

    @POST
    public Response register(Instance instance) {
        if (dispatcher.hasInstance(instance.name)) {
            return Response.status(Status.NOT_MODIFIED).build();
        } else {
            try {
                dispatcher.register(instance);
                return Response.status(Status.CREATED).entity(instance).build();
            } catch (DispatcherException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        }
    }

    @DELETE
    @Path("/{name}")
    public Response unregister(@PathParam("name") String name) {
        try {
            if (dispatcher.unregister(name)) {
                return Response.noContent().build();
            } else {
                return Response.status(Status.NOT_FOUND).entity("No instance found for '" + name + "'.").build();
            }
        } catch (DispatcherException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
