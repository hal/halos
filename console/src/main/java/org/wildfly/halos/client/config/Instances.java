package org.wildfly.halos.client.config;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.EventSource;
import elemental2.dom.EventSourceInit;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;
import org.wildfly.halos.client.util.Logger;

import static elemental2.dom.DomGlobal.fetch;
import static java.util.Arrays.asList;
import static org.wildfly.halos.client.util.Strings.emptyToNull;

@Singleton
public class Instances {

    private final Environment environment;
    private final Endpoints endpoints;
    private final Set<String> instances;
    public String primary;

    @Inject
    public Instances(Environment environment, Endpoints endpoints) {
        this.environment = environment;
        this.endpoints = endpoints;
        this.instances = new HashSet<>();
        this.primary = null;
    }

    public void init() {
        RequestInit request = RequestInit.create();
        if (environment.cors) {
            request.setMode("cors");
        }
        fetch(endpoints.instance, request)
                .then(response -> {
                    if (response.ok) {
                        return response.text();
                    }
                    Logger.error("Unable to get instances: {} {}", response.status, response.statusText);
                    return Promise.resolve("");
                })
                .then(payload -> {
                    if (emptyToNull(payload) != null) {
                        String[] parts = payload.split(",");
                        if (parts.length != 0) {
                            instances.addAll(asList(parts));
                        }
                    }
                    setPrimary();
                    return null;
                });
    }

    public void subscribe() {
        EventSourceInit init = EventSourceInit.create();
        init.setWithCredentials(environment.cors);
        EventSource eventSource = new EventSource(endpoints.instance + "/subscribe", init);

        eventSource.onmessage = event -> {
            if (event.data != null) {
                String payload = String.valueOf(event.data);
                String[] parts = payload.split(",");
                if (parts.length == 2) {
                    if ("ADDED".equals(parts[0])) {
                        instances.add(parts[1]);
                    } else if ("REMOVED".equals(parts[0])) {
                        instances.remove(parts[1]);
                        setPrimary();
                    }
                }
            }
            return null;
        };
        eventSource.onerror = error -> {
            Logger.error("Error subscribing to instance modifications");
            return null;
        };
    }

    private void setPrimary() {
        if (!instances.isEmpty()) {
            primary = instances.iterator().next();
        } else {
            primary = null;
            Logger.warn("Cannot assign primary instance: No instances found!");
        }
    }
}
