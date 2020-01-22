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
package org.jboss.hal.meta.description;

import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;

public class ResourceDescriptionStatementContext extends FilteringStatementContext {

    public ResourceDescriptionStatementContext(StatementContext delegate, Environment environment) {
        super(delegate, new Filter() {
            @Override
            public String filter(String placeholder, AddressTemplate template) {
                if (SELECTION_KEY.equals(placeholder)) {
                    return "*";
                }
                return delegate.resolve(placeholder, template);
            }

            @Override
            public String[] filterTuple(String placeholder, AddressTemplate template) {
                if (!environment.isStandalone()) {
                    Expression t = Expression.from(placeholder);
                    if (t != null) {
                        if (t == Expression.SELECTED_HOST && template.size() == 1) {
                            return delegate.resolveTuple(placeholder, template);
                        }
                        return new String[]{t.resource(), "*"};
                    }
                }
                return delegate.resolveTuple(placeholder, template);
            }
        });
    }
}
