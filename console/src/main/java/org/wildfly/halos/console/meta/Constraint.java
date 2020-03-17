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

import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;

import static org.wildfly.halos.console.meta.Target.ATTRIBUTE;
import static org.wildfly.halos.console.meta.Target.OPERATION;

/** A constraint for an attribute or operation of a DMR resource. */
public class Constraint {

    public static Constraint executable(AddressTemplate template, String operation) {
        return new Constraint(template, operation, OPERATION, Permission.EXECUTABLE);
    }

    public static Constraint writable(AddressTemplate template, String attribute) {
        return new Constraint(template, attribute, ATTRIBUTE, Permission.WRITABLE);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static Constraint parse(String input) throws IllegalArgumentException {
        if (!CONSTRAINT_REGEX.test(input)) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        RegExpResult result = CONSTRAINT_REGEX.exec(input);
        if (result.length != 5) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        return new Constraint(AddressTemplate.of(result.getAt(2)), result.getAt(4),
                Target.parse(result.getAt(3)), Permission.valueOf(result.getAt(1).toUpperCase()));
    }


    private static final JsRegExp CONSTRAINT_REGEX = new JsRegExp(
            "^(readable|writable|executable)\\(([\\w{}=*\\-\\/\\.]+)(:|@)([\\w\\-]+)\\)$"); //NON-NLS

    public final AddressTemplate template;
    public final Target target;
    public final String name;
    public final Permission permission;

    private Constraint(AddressTemplate template, String name, Target target, Permission permission) {
        this.template = template;
        this.target = target;
        this.name = name;
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Constraint)) {
            return false;
        }

        Constraint that = (Constraint) o;
        if (!template.equals(that.template)) {
            return false;
        }
        if (target != that.target) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        return permission == that.permission;
    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + permission.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // Do NOT change the format, Constraint.parseSingle() relies on it!
        return permission.name().toLowerCase() + "(" + template + target.symbol() + name + ")";
    }

    public String data() {
        return toString();
    }
}
