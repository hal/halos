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

public enum RequestHeader {

    // only those which are used in HAL
    ACCEPT("Accept"),
    CONTENT_TYPE("Content-Type"),
    X_MANAGEMENT_CLIENT_NAME("X-Management-Client-Name");

    private final String header;

    RequestHeader(String header) {
        this.header = header;
    }

    public String header() {
        return header;
    }
}
