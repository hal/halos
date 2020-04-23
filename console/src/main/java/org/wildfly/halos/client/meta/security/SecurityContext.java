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

import org.wildfly.halos.client.dmr.ModelNode;

import static org.wildfly.halos.client.dmr.ModelDescriptionConstants.*;

/** Represents the RBAC related payload from the read-resource-description operation. */
public class SecurityContext extends ModelNode {

    /** A security context with hardcoded permissions to read resources, write and execute operations are not allowed. */
    public static final SecurityContext READ_ONLY = new SecurityContext(new ModelNode(), false) {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean isReadable(String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(String attribute) {
            return false;
        }

        @Override
        public boolean isExecutable(String operation) {
            return false;
        }
    };

    /** A security context with hardcoded permissions to read, write and execute any resource. */
    public static final SecurityContext RWX = new SecurityContext(new ModelNode(), false) {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isReadable(String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(String attribute) {
            return true;
        }

        @Override
        public boolean isExecutable(String operation) {
            return true;
        }
    };

    public final boolean recursive;

    public SecurityContext(ModelNode payload, boolean recursive) {
        this.recursive = recursive;
        set(payload);
    }

    /** @return whether the security context is readable */
    public boolean isReadable() {
        return get(READ).asBoolean();
    }

    /** @return whether the security context is writable */
    public boolean isWritable() {
        return get(WRITE).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     * @return whether the attribute is readable
     */
    public boolean isReadable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(READ).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     * @return whether the attribute is writable
     */
    public boolean isWritable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(WRITE).asBoolean();
    }

    /**
     * @param operation The operation to check.
     * @return whether the operation is executable
     */
    public boolean isExecutable(String operation) {
        return hasDefined(OPERATIONS) &&
                get(OPERATIONS).hasDefined(operation) &&
                get(OPERATIONS).get(operation).get(EXECUTE).asBoolean();
    }
}
