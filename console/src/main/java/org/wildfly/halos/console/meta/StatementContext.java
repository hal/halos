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
package org.wildfly.halos.console.meta;

/** Holds global state which can be used when resolving an {@linkplain AddressTemplate address template}. */
public interface StatementContext {

    StatementContext NOOP = new StatementContext() {
        @Override
        public AddressTemplate.Segment resolve(AddressTemplate.Segment segment) {
            return segment;
        }

        @Override
        public void addPlaceholder(Placeholder placeholder) {
            // noop
        }

        @Override
        public Placeholder getPlaceholder(String name) {
            return null;
        }

        @Override
        public String getValue(Placeholder placeHolder) {
            return null;
        }

        @Override
        public void assignValue(String placeholder, String value) {
            // noop
        }
    };

    AddressTemplate.Segment resolve(AddressTemplate.Segment segment);

    void addPlaceholder(Placeholder placeholder);

    Placeholder getPlaceholder(String name);

    String getValue(Placeholder placeHolder);

    void assignValue(String placeholder, String value);
}
