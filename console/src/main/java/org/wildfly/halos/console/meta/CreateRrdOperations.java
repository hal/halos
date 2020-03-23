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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wildfly.halos.console.dmr.Operation;
import org.wildfly.halos.console.dmr.ResourceAddress;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.*;

class CreateRrdOperations {

    private final StatementContext statementContext;
    private final SegmentResolvers resolvers;
    private final String locale;
    private final int depth;

    CreateRrdOperations(StatementContext statementContext, SegmentResolvers resolvers, String locale, int depth) {
        this.statementContext = statementContext;
        this.resolvers = resolvers;
        this.locale = locale;
        this.depth = depth;
    }

    List<Operation> create(Map<AddressTemplate, Metadata> allMetadata, boolean recursive) {
        List<Operation> operations = new ArrayList<>();
        allMetadata.forEach((template, metadata) -> {
            if (metadata.nothingPresent()) {
                ResourceAddress rdAddress = template.resolve(statementContext,
                        resolvers.resourceDescriptionResolver());
                ResourceAddress scAddress = template.resolve(statementContext, resolvers.securityContextResolver());
                if (rdAddress.equals(scAddress)) {
                    Operation.Builder builder = new Operation.Builder(rdAddress,
                            READ_RESOURCE_DESCRIPTION_OPERATION)
                            .param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS)
                            .param(OPERATIONS, true);
                    operations.add(addLocaleAndDepth(builder, recursive).build());
                } else {
                    Operation.Builder rdBuilder = new Operation.Builder(rdAddress,
                            READ_RESOURCE_DESCRIPTION_OPERATION)
                            .param(OPERATIONS, true);
                    Operation.Builder scBuilder = new Operation.Builder(scAddress,
                            READ_RESOURCE_DESCRIPTION_OPERATION)
                            .param(ACCESS_CONTROL, TRIM_DESCRIPTIONS)
                            .param(OPERATIONS, true);
                    operations.add(addLocaleAndDepth(rdBuilder, recursive).build());
                    operations.add(addLocaleAndDepth(scBuilder, recursive).build());
                }

            } else if (metadata.description == null) {
                ResourceAddress address = template.resolve(statementContext,
                        resolvers.resourceDescriptionResolver());
                Operation.Builder builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(OPERATIONS, true);
                operations.add(addLocaleAndDepth(builder, recursive).build());

            } else if (metadata.securityContext == null) {
                ResourceAddress address = template.resolve(statementContext, resolvers.securityContextResolver());
                Operation.Builder builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(ACCESS_CONTROL, TRIM_DESCRIPTIONS)
                        .param(OPERATIONS, true);
                operations.add(addLocaleAndDepth(builder, recursive).build());

            }
        });
        return operations;
    }

    private Operation.Builder addLocaleAndDepth(Operation.Builder builder, boolean recursive) {
        if (recursive) {
            builder.param(RECURSIVE_DEPTH, depth);
        }
        builder.param(LOCALE, locale);
        return builder;
    }
}
