package org.wildfly.halos.console.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.location;
import static org.wildfly.halos.console.util.Strings.stripEnd;

@Singleton
public class Endpoints {

    public final String instance;
    public final String management;

    @Inject
    public Endpoints(Environment environment) {
        String baseUrl;
        if (environment.proxyUrl != null) {
            baseUrl = stripEnd(environment.proxyUrl, "/") + "/" + environment.restVersion;
        } else {
            baseUrl = location.getProtocol() + "//" + location.getHash() + "/" + environment.restVersion;
        }
        this.instance = baseUrl + "/instance";
        this.management = baseUrl + "/management";
    }
}
