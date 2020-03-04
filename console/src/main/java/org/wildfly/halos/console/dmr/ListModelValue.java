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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class ListModelValue extends ModelValue {

    public static final ModelNode[] NO_NODES = new ModelNode[0];
    private final List<ModelNode> list;

    ListModelValue() {
        super(ModelType.LIST);
        list = new ArrayList<>();
    }

    private ListModelValue(ListModelValue orig) {
        super(ModelType.LIST);
        list = new ArrayList<>(orig.list);
    }

    ListModelValue(List<ModelNode> list) {
        super(ModelType.LIST);
        this.list = list;
    }

    ListModelValue(DataInput in) {
        super(ModelType.LIST);
        int count = in.readInt();
        ArrayList<ModelNode> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ModelNode value = new ModelNode();
            value.readExternal(in);
            list.add(value);
        }
        this.list = list;
    }

    @Override
    void writeExternal(DataOutput out) {
        List<ModelNode> list = this.list;
        int size = list.size();
        out.writeInt(size);
        for (ModelNode node : list) {
            node.writeExternal(out);
        }
    }

    @Override
    ModelValue protect() {
        List<ModelNode> list = this.list;
        for (ModelNode node : list) {
            node.protect();
        }
        return list.getClass() == ArrayList.class ? new ListModelValue(Collections.unmodifiableList(list)) : this;
    }

    @Override
    long asLong() {
        return asInt();
    }

    @Override
    long asLong(long defVal) {
        return asInt();
    }

    @Override
    int asInt() {
        return list.size();
    }

    @Override
    int asInt(int defVal) {
        return asInt();
    }

    @Override
    boolean asBoolean() {
        return !list.isEmpty();
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return asBoolean();
    }

    @Override
    Property asProperty() {
        if (list.size() == 2) {
            return new Property(list.get(0).asString(), list.get(1));
        } else {
            return super.asProperty();
        }
    }

    @Override
    List<Property> asPropertyList() {
        List<Property> propertyList = new ArrayList<>();
        Iterator<ModelNode> i = list.iterator();
        while (i.hasNext()) {
            ModelNode node = i.next();
            if (node.getType() == ModelType.PROPERTY) {
                propertyList.add(node.asProperty());
            } else if (i.hasNext()) {
                ModelNode value = i.next();
                propertyList.add(new Property(node.asString(), value));
            }
        }
        return propertyList;
    }

    @Override
    ModelNode asObject() {
        ModelNode node = new ModelNode();
        Iterator<ModelNode> i = list.iterator();
        while (i.hasNext()) {
            ModelNode name = i.next();
            if (name.getType() == ModelType.PROPERTY) {
                Property property = name.asProperty();
                node.get(property.getName()).set(property.getValue());
            } else if (i.hasNext()) {
                ModelNode value = i.next();
                node.get(name.asString()).set(value);
            }
        }
        return node;
    }

    @Override
    ModelNode getChild(int index) {
        List<ModelNode> list = this.list;
        int size = list.size();
        if (size <= index) {
            for (int i = 0; i < index - size + 1; i++) {
                list.add(new ModelNode());
            }
        }
        return list.get(index);
    }

    @Override
    ModelNode addChild() {
        ModelNode node = new ModelNode();
        list.add(node);
        return node;
    }

    @Override
    List<ModelNode> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    ModelValue copy() {
        return new ListModelValue(this);
    }

    @Override
    ModelValue resolve() {
        ArrayList<ModelNode> copy = new ArrayList<>(list.size());
        for (ModelNode node : list) {
            copy.add(node.resolve());
        }
        return new ListModelValue(copy);
    }

    @Override
    String asString() {
        StringBuilder builder = new StringBuilder();
        format(builder, 0, false);
        return builder.toString();
    }

    @Override
    void format(StringBuilder builder, int indent, boolean multiLineRequested) {
        boolean multiLine = multiLineRequested && list.size() > 1;
        List<ModelNode> list = asList();
        Iterator<ModelNode> iterator = list.iterator();
        builder.append('[');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        while (iterator.hasNext()) {
            ModelNode entry = iterator.next();
            entry.format(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(',');
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append(']');
    }

    @Override
    void formatAsJSON(StringBuilder builder, int indent, boolean multiLineRequested) {
        boolean multiLine = multiLineRequested && list.size() > 1;
        List<ModelNode> list = asList();
        Iterator<ModelNode> iterator = list.iterator();
        builder.append('[');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        while (iterator.hasNext()) {
            ModelNode entry = iterator.next();
            entry.formatAsJSON(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(',');
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append(']');
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof ListModelValue && equals((ListModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(ListModelValue other) {
        return this == other || other != null && list.equals(other.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    boolean has(int index) {
        return 0 <= index && index < list.size();
    }

    @Override
    ModelNode requireChild(int index) throws NoSuchElementException {
        try {
            return list.get(index);
        } catch (IndexOutOfBoundsException ignored) {
            return super.requireChild(index);
        }
    }
}
