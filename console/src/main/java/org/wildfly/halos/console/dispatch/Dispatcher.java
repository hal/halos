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
package org.wildfly.halos.console.dispatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.dmr.Composite;
import org.wildfly.halos.console.dmr.CompositeResult;
import org.wildfly.halos.console.dmr.ModelNode;
import org.wildfly.halos.console.dmr.Operation;

import static elemental2.dom.DomGlobal.fetch;
import static org.wildfly.halos.console.dispatch.RequestHeader.ACCEPT;
import static org.wildfly.halos.console.dispatch.RequestHeader.CONTENT_TYPE;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.RESULT;

/** Executes operations against the management endpoint. */
@Singleton
public class Dispatcher {

    static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    // private final Endpoints endpoints;

    @Inject
    public Dispatcher() {
        // this.endpoints = endpoints;
    }

    // ------------------------------------------------------ execute

    public Promise<ModelNode> execute(Operation operation) {
        return dmr(operation);
    }

    public Promise<CompositeResult> execute(Composite operations) {
        return dmr(operations)
                .then(modelNode -> new Promise<>((resolve, reject) -> resolve.onInvoke(compositeResult(modelNode))));
    }

    private CompositeResult compositeResult(ModelNode modelNode) {
        return new CompositeResult(modelNode.get(RESULT));
    }

    // ------------------------------------------------------ dmr

    private Promise<ModelNode> dmr(Operation operation) {
        Headers headers = new Headers();
        headers.append(ACCEPT.header(), APPLICATION_DMR_ENCODED);
        headers.append(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);

        RequestInit init = RequestInit.create();
        init.setMode("cors");
        init.setMethod("POST");
        init.setHeaders(headers);
        init.setBody(operation.toBase64String());

        return fetch("http://localhost:8080/v1/management", init)
                .then(response -> {
                    if (response.ok) {
                        return response.text();
                    } else {
                        throw new DispatcherException("Error executing operation '" + operation.asCli() + "': " +
                                response.status + " " + response.statusText);
                    }
                })
                .then(payload -> new Promise<>((resolve, reject) -> resolve.onInvoke(ModelNode.fromBase64(payload))));
    }
}
