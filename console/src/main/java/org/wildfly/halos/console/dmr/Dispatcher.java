/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.halos.console.dmr;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.config.Endpoints;
import org.wildfly.halos.console.config.Environment;

import static elemental2.dom.DomGlobal.fetch;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.RESULT;

/** Executes operations against the management endpoint. */
@Singleton
public class Dispatcher {

    private static final String ACCEPT = "Accept";
    private static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    private static final String CONTENT_TYPE = "Content-Type";

    private final Environment environment;
    private final Endpoints endpoints;

    @Inject
    public Dispatcher(Environment environment, Endpoints endpoints) {
        this.environment = environment;
        this.endpoints = endpoints;
    }

    // ------------------------------------------------------ execute

    public Promise<ModelNode> execute(Operation operation) {
        return dmr(operation);
    }

    public Promise<CompositeResult> execute(Composite composite) {
        return dmr(composite).then(modelNode -> {
            ModelNode steps = modelNode.get(RESULT);
            return Promise.resolve(new CompositeResult(composite, steps));
        });
    }

    // ------------------------------------------------------ internals

    private Promise<ModelNode> dmr(Operation operation) {
        Headers headers = new Headers();
        headers.append(ACCEPT, APPLICATION_DMR_ENCODED);
        headers.append(CONTENT_TYPE, APPLICATION_DMR_ENCODED);

        RequestInit request = RequestInit.create();
        if (environment.cors) {
            request.setMode("cors");
        }
        request.setMethod("POST");
        request.setHeaders(headers);
        request.setBody(operation.toBase64String());

        return fetch(endpoints.management, request)
                .then(response -> {
                    if (response.ok) {
                        return response.text();
                    } else {
                        throw new DispatcherException("Error executing operation '" + operation.asCli() + "': " +
                                response.status + " " + response.statusText);
                    }
                })
                .then(payload -> Promise.resolve(ModelNode.fromBase64(payload)));
    }
}
