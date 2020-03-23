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
package org.wildfly.halos.console.dmr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.COMPOSITE;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.STEPS;

/** Represents a composite operation consisting of n {@link Operation}s. */
public class Composite extends Operation implements Iterable<Operation> {

    private List<Operation> operations;

    /** Creates a new empty composite. */
    public Composite() {
        this(ResourceAddress.root());
    }

    public Composite(ResourceAddress address) {
        super(COMPOSITE, address, new ModelNode(), new ModelNode(), emptySet());
        this.operations = new ArrayList<>();
    }

    public Composite(Operation first, Operation... rest) {
        this(ResourceAddress.root()); // required by JsInterop
        add(first);
        if (rest != null) {
            for (Operation operation : rest) {
                add(operation);
            }
        }
    }

    public Composite(List<Operation> operations) {
        this(ResourceAddress.root());
        operations.forEach(this::add);
    }

    /**
     * Adds the specified operation to this composite.
     *
     * @param operation The operation to add.
     *
     * @return this composite
     */
    public Composite add(Operation operation) {
        operations.add(operation);
        get(STEPS).add(operation);
        return this;
    }

    @Override
    public Iterator<Operation> iterator() {
        return operations.iterator();
    }

    /** @return whether this composite contains operations */
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    /** @return the number of operations */
    public int size() {
        return operations.size();
    }

    @Override
    public Operation get(int index) {return operations.get(index);}

    /** @return a string representation of this composite */
    @Override
    public String toString() {
        return "Composite(" + operations.size() + ")";
    }

    /** @return the string representation of the operation as used in the CLI */
    public String asCli() {
        return operations.stream().map(Operation::asCli).collect(joining("\n"));
    }
}
