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
package org.jboss.hal.dmr.macro;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.wildfly.halos.resources.Ids;

import static java.util.stream.Collectors.joining;

public class Macro {

    private String name;
    private String description;
    private List<Operation> operations;
    private boolean sealed;

    public Macro(String name, String description) {
        this.name = name;
        this.description = description;
        this.operations = new ArrayList<>();
        this.sealed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Macro)) {
            return false;
        }

        Macro macro = (Macro) o;
        return name.equals(macro.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Macro(" + name + ", " + (sealed ? "sealed" : "recording") + ")";
    }

    String getId() {
        return Ids.build(Ids.MACRO_STORAGE, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addOperation(Operation operation) {
        if (sealed) {
            throw new IllegalStateException("Macro is sealed");
        }
        this.operations.add(operation);
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public int getOperationCount() {
        int count = 0;
        for (Operation operation : operations) {
            count += operation instanceof Composite ? ((Composite) operation).size() : 1;
        }
        return count;
    }

    public String asCli() {
        return getOperations().stream().map(Operation::asCli).collect(joining("\n"));
    }

    public void seal() {
        this.sealed = true;
    }

    public boolean isSealed() {
        return sealed;
    }
}
