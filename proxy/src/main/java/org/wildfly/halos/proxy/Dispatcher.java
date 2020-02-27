package org.wildfly.halos.proxy;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import com.sun.jdi.JDIPermission;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.logging.Logger;

/** Manages connections to the WildFly management endpoints and execute DMR operations. */
@ApplicationScoped
public class Dispatcher {

    private static final String REMOTE_HTTP = "remote+http";
    private static Logger log = Logger.getLogger("halos.proxy.dispatcher");

    private final Map<String, ModelControllerClient> clients;

    @Inject
    public Dispatcher() {
        this.clients = new HashMap<>();
    }

    public void register(Instance instance) throws DispatcherException {
        try {
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
            clients.put(instance.name, client);
            log.infof("Created client for %s", instance);
        } catch (UnknownHostException e) {
            String error = String.format("Unable to connect to %s: %s", instance, e.getLocalizedMessage());
            log.error(error);
            throw new DispatcherException(error, e);
        }
    }

    public void unregister(Instance instance) throws DispatcherException {
        ModelControllerClient client = clients.remove(instance.name);
        if (client != null) {
            try {
                client.close();
                log.infof("Closed client for %s", instance);
            } catch (IOException e) {
                String error = String.format("Unable to close client for %s: %s", instance, e.getLocalizedMessage());
                log.error(error);
                throw new DispatcherException(error, e);
            }
        }
    }
}
