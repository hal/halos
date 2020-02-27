package org.wildfly.halos.proxy;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
    private final Map<String, Instance> instances;

    public InstanceResource() {
        instances = new HashMap<>();
    }

    @POST
    public Response register(Instance instance) {
        try {
            dispatcher.register(instance);
            instances.put(instance.name, instance);
            return Response.status(Status.CREATED).entity(instance).build();
        } catch (DispatcherException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{name}")
    public Response unregister(@PathParam("name") String name) {
        Instance instance = instances.remove(name);
        if (instance != null) {
            try {
                dispatcher.unregister(instance);
                return Response.noContent().build();
            } catch (DispatcherException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Status.NOT_FOUND).entity("No instance found for '" + name + "'").build();
        }
    }
}
