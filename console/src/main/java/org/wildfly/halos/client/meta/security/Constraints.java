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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.wildfly.halos.client.util.Strings.emptyToNull;

/** A set of {@linkplain Constraint constraints} with an operator. */
public class Constraints implements Iterable<Constraint> {

    public enum Operator {
        AND("&"), OR("|");

        private final String operator;

        Operator(String operator) {
            this.operator = operator;
        }

        public String operator() {
            return operator;
        }
    }


    // ------------------------------------------------------ factory methods

    public static Constraints single(Constraint constraint) {
        LinkedHashSet<Constraint> set = new LinkedHashSet<>();
        set.add(constraint);
        return new Constraints(set, Operator.AND);
    }

    public static Constraints and(Constraint first, Constraint... rest) {
        LinkedHashSet<Constraint> set = new LinkedHashSet<>();
        set.add(first);
        if (rest != null) {
            set.addAll(asList(rest));
        }
        return new Constraints(set, Operator.AND);
    }

    public static Constraints and(Iterable<Constraint> constraints) {
        LinkedHashSet<Constraint> set = new LinkedHashSet<>();
        for (Constraint constraint : constraints) {
            set.add(constraint);
        }
        return new Constraints(set, Operator.AND);
    }

    public static Constraints or(Constraint first, Constraint... rest) {
        LinkedHashSet<Constraint> set = new LinkedHashSet<>();
        set.add(first);
        if (rest != null) {
            Collections.addAll(set, rest);
        }
        return new Constraints(set, Operator.OR);
    }

    public static Constraints or(Iterable<Constraint> constraints) {
        LinkedHashSet<Constraint> set = new LinkedHashSet<>();
        for (Constraint constraint : constraints) {
            set.add(constraint);
        }
        return new Constraints(set, Operator.OR);
    }

    /** Creates an empty instance using {@link Operator#AND} as operator. */
    public static Constraints empty() {
        return new Constraints(new LinkedHashSet<>(), Operator.AND);
    }

    // ------------------------------------------------------ parse

    public static Constraints parse(String input) {
        if (emptyToNull(input) != null) {
            Operator operator;
            if (input.contains(Operator.AND.operator)) {
                operator = Operator.AND;
            } else if (input.contains(Operator.OR.operator)) {
                operator = Operator.OR;
            } else {
                operator = Operator.AND;
            }
            String[] values = input.split(operator.operator);
            LinkedHashSet<Constraint> constraints = new LinkedHashSet<>();
            for (String value : values) {
                try {
                    constraints.add(Constraint.parse(value));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return new Constraints(constraints, operator);

        } else {
            return empty();
        }
    }

    // ------------------------------------------------------ instance

    private final LinkedHashSet<Constraint> constraints;
    public final Operator operator;

    private Constraints(LinkedHashSet<Constraint> constraints, Operator operator) {
        this.constraints = constraints;
        this.operator = operator;
    }

    @Override
    public String toString() {
        // Do NOT change the format, Constraint.parseSingle() relies on it!
        if (!constraints.isEmpty()) {
            if (constraints.size() == 1) {
                return constraints.iterator().next().toString();
            }
            return constraints.stream().map(Constraint::toString).collect(joining(operator.operator()));
        }
        return "";
    }

    public String data() {
        return toString();
    }

    @Override
    public Iterator<Constraint> iterator() {
        return constraints.iterator();
    }

    public int size() {
        return constraints.size();
    }

    public boolean isEmpty() {
        return constraints.isEmpty();
    }

    public boolean contains(Object o) {
        return constraints.contains(o);
    }

    public Set<Constraint> constraints() {
        return unmodifiableSet(constraints);
    }
}
