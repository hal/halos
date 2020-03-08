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

import java.util.function.Function;

/** Intercepts the resolution and allows to resolve certain values. */
public class LocalStatementContext implements StatementContext {

    private Function<AddressTemplate.Segment, String> interceptor;
    private StatementContext delegate;

    public LocalStatementContext(StatementContext delegate, Function<AddressTemplate.Segment, String> interceptor) {
        this.delegate = delegate;
        this.interceptor = interceptor;
    }

    @Override
    public String resolve(AddressTemplate.Segment segment) {
        String resolved = interceptor.apply(segment);
        return resolved != null ? resolved : delegate.resolve(segment);
    }
}
