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
package org.wildfly.halos.console.config;

import javax.inject.Inject;
import javax.inject.Singleton;

/** Class holding information about halOS. */
@Singleton
public class Environment {

    private final Endpoints endpoints;
    public final Version version;
    public final boolean cors;
    public final String proxyUrl;
    public final String restVersion;
    public AccessControlProvider accessControlProvider;

    @Inject
    public Environment(Endpoints endpoints) {
        this.endpoints = endpoints;
        this.cors = Boolean.getBoolean(System.getProperty("halos.cors", "true"));
        this.proxyUrl = System.getProperty("halos.proxy.url");
        this.restVersion = System.getProperty("halos.rest.version");
        this.version = Version.parseVersion(System.getProperty("halos.rest.version"));
        this.accessControlProvider = AccessControlProvider.SIMPLE;
    }

    public void init() {
        // TODO fetch environment from servers
    }
}
