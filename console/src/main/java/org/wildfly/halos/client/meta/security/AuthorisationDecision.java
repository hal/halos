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
package org.wildfly.halos.client.meta.security;

import java.util.BitSet;

import org.wildfly.halos.client.config.AccessControlProvider;
import org.wildfly.halos.client.config.Environment;
import org.wildfly.halos.client.util.Logger;

import static org.wildfly.halos.client.meta.security.Constraints.Operator.AND;

/**
 * Class to decide whether a single, or a set of constraints are allowed according to a given security context. {@code
 * isAllowed()} returns {@code true} if the security context was resolved, and the constraint is valid.
 * <p>
 * To hide or disable UI elements, use one of the following strategies:
 * <dl>
 * <dt>Eager filtering</dt>
 * <dd>If the security context is <strong>available</strong> when the UI elements are created, filter the elements
 * based on the outcome of {@code isAllowed()}. Add only allowed elements to the DOM.</dd>
 * <dt>Late hiding</dt>
 * <dd>If the security context is <strong>not</strong> available when the UI elements are created, store the
 * constraints as {@code data-constraint} attributes. Later when you have access to the security context
 * post-process the elements using one of the {@code processElements()} method from {@link ElementGuard}.</dd>
 * </dl>
 * <p>
 * If WildFly uses {@link org.wildfly.halos.client.config.AccessControlProvider#SIMPLE}, {@code isAllowed()} will <strong>always</strong>
 * return {@code true}.
 */
public class AuthorisationDecision {

    private final Environment environment;
    private final SecurityContext securityContext;

    public AuthorisationDecision(Environment environment, SecurityContext securityContext) {
        this.environment = environment;
        this.securityContext = securityContext;
    }

    public boolean isAllowed(Constraints constraints) {
        if (environment.accessControlProvider == AccessControlProvider.SIMPLE || constraints.isEmpty()) {
            return true;
        }

        int size = constraints.size();
        if (size == 1) {
            return isAllowed(constraints.iterator().next());
        } else {
            int index = 0;
            BitSet bits = new BitSet(size);
            for (Constraint constraint : constraints) {
                bits.set(index, isAllowed(constraint));
                index++;
            }
            int cardinality = bits.cardinality();
            return constraints.operator == AND ? cardinality == size : cardinality > 0;
        }
    }

    public boolean isAllowed(Constraint constraint) {
        if (environment.accessControlProvider == AccessControlProvider.SIMPLE) {
            return true;
        }

        boolean allowed = false;
        if (constraint.target == Target.OPERATION) {
            switch (constraint.permission) {
                case EXECUTABLE:
                    allowed = securityContext.isExecutable(constraint.name);
                    break;
                case READABLE:
                case WRITABLE:
                    Logger.error("Unsupported permission in constraint {}. Only {} is allowed for target {}.",
                            constraint, Permission.EXECUTABLE.name().toLowerCase(),
                            Target.OPERATION.name().toLowerCase());
                    break;
                default:
                    break;
            }

        } else if (constraint.target == Target.ATTRIBUTE) {
            switch (constraint.permission) {
                case READABLE:
                    allowed = securityContext.isReadable(constraint.name);
                    break;
                case WRITABLE:
                    allowed = securityContext.isWritable(constraint.name);
                    break;
                case EXECUTABLE:
                    Logger.error("Unsupported permission in constraint {}. Only ({}|{}) are allowed for target {}.",
                            constraint, Permission.READABLE.name().toLowerCase(),
                            Permission.WRITABLE.name().toLowerCase(),
                            Target.ATTRIBUTE.name().toLowerCase());
                    break;
                default:
                    break;
            }
        }
        return allowed;
    }
}
