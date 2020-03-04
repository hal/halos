package org.wildfly.halos.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/** Manages connections to the WildFly management endpoints and execute DMR operations. */
@ApplicationScoped
class Dispatcher {

    private static final String REMOTE_HTTP = "remote+http";
    private static Logger log = Logger.getLogger("halos.proxy.dispatcher");

    private final SortedMap<Instance, ModelControllerClient> clients;

    @Inject
    Dispatcher() {
        this.clients = new TreeMap<>();
    }

    void register(Instance instance) throws DispatcherException {
        try {
            InetAddress address = InetAddress.getByName(instance.host);
            ModelControllerClient client = ModelControllerClient.Factory.create(REMOTE_HTTP, address,
                    instance.port, callbacks -> {
                        for (Callback current : callbacks) {
                            if (current instanceof NameCallback) {
                                NameCallback ncb = (NameCallback) current;
                                ncb.setName(instance.username);
                            } else if (current instanceof PasswordCallback) {
                                PasswordCallback pcb = (PasswordCallback) current;
                                pcb.setPassword(instance.password.toCharArray());
                            } else if (current instanceof RealmCallback) {
                                RealmCallback rcb = (RealmCallback) current;
                                rcb.setText(rcb.getDefaultText());
                            } else {
                                throw new UnsupportedCallbackException(current);
                            }
                        }
                    });
            clients.put(instance, client);
            log.infof("Created client for %s", instance);
        } catch (UnknownHostException e) {
            String error = String.format("Unable to connect to instance %s: %s", instance, e.getMessage());
            log.error(error);
            throw new DispatcherException(error, e);
        }
    }

    boolean unregister(String name) throws DispatcherException {
        Map.Entry<Instance, ModelControllerClient> entry = findByName(name);
        if (entry != null) {
            Instance instance = entry.getKey();
            ModelControllerClient client = entry.getValue();
            try {
                client.close();
                log.infof("Closed client for %s", instance);
                return true;
            } catch (IOException e) {
                String error = String.format("Unable to close client for %s: %s", instance, e.getMessage());
                log.error(error);
                throw new DispatcherException(error, e);
            } finally {
                clients.remove(instance);
            }
        }
        return false;
    }

    boolean isEmpty() {
        return clients.isEmpty();
    }

    boolean hasInstance(String name) {
        return findByName(name) != null;
    }

    Iterable<Instance> instances() {
        return clients.keySet();
    }

    ModelNode execute(Operation operation) {
        ModelNode result = new ModelNode();
        clients.forEach((instance, client) -> wrapResult(result, instance, client, operation));
        return result;
    }

    ModelNode executeSingle(String name, Operation operation) {
        Map.Entry<Instance, ModelControllerClient> entry = findByName(name);
        if (entry != null) {
            ModelNode result = new ModelNode();
            wrapResult(result, entry.getKey(), entry.getValue(), operation);
            return result;
        } else {
            log.errorf("Unable to find client for instance %1s. Did you register %1s?", name);
            return null;
        }
    }

    private void wrapResult(ModelNode container, Instance instance, ModelControllerClient client, Operation operation) {
        ModelNode result;
        try {
            result = client.execute(operation);
        } catch (IOException e) {
            log.errorf("Error executing operation %s against %s: %s",
                    operation.getOperation().toJSONString(true), instance, e.getMessage());
            result = new ModelNode();
            result.get(ClientConstants.OUTCOME).set("failed");
            result.get(ClientConstants.FAILURE_DESCRIPTION).set(e.getMessage());
        }
        container.get(instance.name).set(result);
    }

    private Map.Entry<Instance, ModelControllerClient> findByName(String name) {
        for (Map.Entry<Instance, ModelControllerClient> entry : clients.entrySet()) {
            if (name.equals(entry.getKey().name)) {
                return entry;
            }
        }
        return null;
    }
}
