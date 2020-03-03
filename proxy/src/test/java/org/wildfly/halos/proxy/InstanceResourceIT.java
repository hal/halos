package org.wildfly.halos.proxy;

import javax.ws.rs.core.MediaType;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class InstanceResourceIT {

    @Test
    public void registerExisting() {
        Instance instance = instance("wf0", 9990);
        Instance result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(instance)
                .when().post("/v1/instance")
                .then()
                .statusCode(200)
                .extract().as(Instance.class);
        Assertions.assertEquals(instance, result);
    }

    @Test
    public void registerInvalid() {
        Instance instance = instance("n/a", 1234);
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(instance)
                .when().post("/v1/instance")
                .then()
                .statusCode(500);
    }

    private Instance instance(String name, int port) {
        Instance instance = new Instance();
        instance.name = name;
        instance.ip = "localhost";
        instance.port = port;
        instance.username = "admin";
        instance.password = "admin";
        return instance;
    }
}
