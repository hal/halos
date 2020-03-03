package org.wildfly.halos.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Multi;
import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.jboss.resteasy.annotations.SseElementType;

@Path("/v1/management")
public class ManagementResource {

    @Inject
    Dispatcher dispatcher;

    @POST
    @Consumes("application/dmr-encoded")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("application/dmr-encoded")
    public Multi<String> execute(InputStream inputStream) {
        try {
            ModelNode modelNode = ModelNode.fromBase64(inputStream);
            Operation operation = Operation.Factory.create(modelNode);
            return dispatcher.execute(operation).onItem().apply(this::base64);
        } catch (IOException e) {
            DispatcherException exception = new DispatcherException("Unable to read operation: " + e.getMessage(), e);
            return Multi.createFrom().failure(exception);
        }
    }

    @POST
    @Path("/{name}")
    @Consumes("application/dmr-encoded")
    @Produces("application/dmr-encoded")
    public Response executeSingle(@PathParam("name") String name, InputStream inputStream) {
        try {
            ModelNode modelNode = ModelNode.fromBase64(inputStream);
            Operation operation = Operation.Factory.create(modelNode);
            ModelNode result = dispatcher.executeSingle(name, operation);
            if (result != null) {
                return Response.ok(base64(result)).build();
            } else {
                return Response.status(Status.NOT_FOUND).entity("Instance " + name + " not found").build();
            }
        } catch (IOException e) {
            return Response.serverError().entity("Unable to read operation: " + e.getMessage()).build();
        }
    }

    private String base64(ModelNode modelNode) {
        String base64 = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            modelNode.writeBase64(out);
            base64 = out.toString();
        } catch (IOException ignored) {
            // ByteArrayOutputStream should not throw exceptions
        }
        return base64;
    }
}
