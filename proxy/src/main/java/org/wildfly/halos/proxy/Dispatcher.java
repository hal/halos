package org.wildfly.halos.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import io.smallrye.mutiny.Multi;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/** Manages connections to the WildFly management endpoints and execute DMR operations. */
@ApplicationScoped
class Dispatcher {

    private static final String REMOTE_HTTP = "remote+http";
    private static Logger log = Logger.getLogger("halos.proxy.dispatcher");

    private final Map<Instance, ModelControllerClient> clients;

    @Inject
    Dispatcher() {
        this.clients = new HashMap<>();
    }

    void register(Instance instance) throws DispatcherException {
        ModelControllerClient client = ModelControllerClient.Factory.create(REMOTE_HTTP, instance.ip,
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
    }

    boolean unregister(String name) throws DispatcherException {
        Map.Entry<Instance, ModelControllerClient> entry = findByName(name);
        if (entry != null) {
            try {
                entry.getValue().close();
                log.infof("Closed client for %s", entry.getValue());
                return true;
            } catch (IOException e) {
                String error = String.format("Unable to close client for %s: %s", entry.getKey(), e.getMessage());
                log.error(error);
                throw new DispatcherException(error, e);
            }
            finally {
                clients.remove(entry.getKey());
            }
        }
        return false;
    }

    Iterable<Instance> instances() {
        return clients.keySet();
    }

    Multi<ModelNode> execute(Operation operation) {
        return Multi.createFrom().emitter(emitter -> {
            for (Map.Entry<Instance, ModelControllerClient> entry : clients.entrySet()) {
                Instance instance = entry.getKey();
                ModelControllerClient client = entry.getValue();
                try {
                    emitter.emit(wrapInstance(instance, client.execute(operation)));
                } catch (IOException e) {
                    log.errorf("Error executing operation %s against %s: %s",
                            operation.getOperation().toJSONString(true), instance, e.getMessage());
                    emitter.fail(e);
                }
            }
            emitter.complete();
        });
    }

    ModelNode executeSingle(String name, Operation operation) {
        Map.Entry<Instance, ModelControllerClient> entry = findByName(name);
        if (entry != null) {
            try {
                return wrapInstance(entry.getKey(), entry.getValue().execute(operation));
            } catch (IOException e) {
                String error = String.format("Error executing operation %s against %s: %s",
                        operation.getOperation().toJSONString(true), entry.getKey(), e.getMessage());
                log.error(error);
                throw new DispatcherException(error, e);
            }
        }
        log.errorf("Unable to find client for instance %1s. Did you register %1s?", name);
        return null;
    }

    private Map.Entry<Instance, ModelControllerClient> findByName(String name) {
        for (Map.Entry<Instance, ModelControllerClient> entry : clients.entrySet()) {
            if (name.equals(entry.getKey().name)) {
                return entry;
            }
        }
        return null;
    }

    private ModelNode wrapInstance(Instance instance, ModelNode modelNode) {
        ModelNode result = new ModelNode();
        result.get(instance.name).set(modelNode);
        return result;
    }
}
