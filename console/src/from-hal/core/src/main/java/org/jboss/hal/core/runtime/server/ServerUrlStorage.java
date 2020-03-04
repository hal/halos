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
package org.jboss.hal.core.runtime.server;

import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import org.wildfly.halos.console.resources.Ids;

import static elemental2.dom.DomGlobal.window;

public class ServerUrlStorage {

    private final Storage storage;

    public ServerUrlStorage() {
        storage = WebStorageWindow.of(window).localStorage;
    }

    String load(String host, String server) {
        if (storage != null) {
            return storage.getItem(id(host, server));
        }
        return null;
    }

    boolean hasUrl(String host, String server) {
        return load(host, server) != null;
    }

    void save(String host, String server, String url) {
        if (storage != null) {
            storage.setItem(id(host, server), url);
        }
    }

    void remove(String host, String server) {
        storage.removeItem(id(host, server));
    }

    private String id(String host, String server) {
        return Ids.build(Ids.SERVER_URL_STORAGE, Ids.hostServer(host, server));
    }
}
