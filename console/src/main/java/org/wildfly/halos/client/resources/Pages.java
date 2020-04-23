package org.wildfly.halos.client.resources;

public interface Pages {

    String BROWSER = "browser";
    String HOME = "home";
    String SERVER = "server";

    static String hash(String page) {
        return "#" + page;
    }
}
