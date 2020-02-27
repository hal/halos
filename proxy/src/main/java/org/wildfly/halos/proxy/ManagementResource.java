package org.wildfly.halos.proxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/v1/management")
@Consumes("application/dmr-encoded")
@Produces("application/dmr-encoded")
public class ManagementResource {

    @POST
    public void dmr() {

    }
}
