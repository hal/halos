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
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.HAL_INDEX;

/**
 * Static helper methods for dealing with {@link ModelNode}s and {@link NamedNode}s. Some methods accept a path
 * parameter separated by "/" to get a deeply nested data.
 */
public class ModelNodeHelper {

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with ".".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "."
     * @return The nested node, or an undefined model node.
     */
    public static ModelNode failSafeGet(ModelNode modelNode, String path) {
        ModelNode undefined = new ModelNode();

        if (path != null && path.length() != 0) {
            String[] segments = path.split("\\.");
            if (segments.length > 0) {
                ModelNode context = modelNode;
                for (String segment : segments) {
                    if (context.hasDefined(segment)) {
                        context = context.get(segment);
                    } else {
                        break;
                    }
                    return context;
                }
            }
        }
        return undefined;
    }

    /**
     * Tries to get a deeply nested boolean value from the specified model node. Nested paths must be separated with
     * "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     * @return the boolean value or false.
     */
    public static boolean failSafeBoolean(ModelNode modelNode, String path) {
        ModelNode attribute = failSafeGet(modelNode, path);
        return attribute.isDefined() && attribute.asBoolean();
    }

    public static List<ModelNode> failSafeList(ModelNode modelNode, String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asList() : Collections.emptyList();
    }

    public static List<Property> failSafePropertyList(ModelNode modelNode, String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asPropertyList() : Collections.emptyList();
    }

    public static <T> T getOrDefault(ModelNode modelNode, String attribute, Supplier<T> supplier, T defaultValue) {
        T result = defaultValue;
        if (modelNode != null && modelNode.hasDefined(attribute)) {
            try {
                result = supplier.get();
            } catch (Throwable ignored) {
                result = defaultValue;
            }
        }
        return result;
    }

    public static void storeIndex(List<ModelNode> modelNodes) {
        int index = 0;
        for (ModelNode modelNode : modelNodes) {
            modelNode.get(HAL_INDEX).set(index);
            index++;
        }
    }

    /**
     * Turns a list of properties into a list of named model nodes which contains a {@link
     * ModelDescriptionConstants#NAME} key with the properties name.
     */
    public static List<NamedNode> asNamedNodes(List<Property> properties) {
        return properties.stream().map(NamedNode::new).collect(toList());
    }

    /**
     * Looks for the specified attribute and tries to convert it to an enum constant using {@code
     * LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.get(attribute).asString())}.
     */
    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNode, String attribute, Function<String, E> valueOf,
            E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNode, Function<String, E> valueOf, E defaultValue) {
        E value = defaultValue;
        String converted = modelNode.asString().toUpperCase();
        converted = converted.replace('-', '_');
        try {
            value = valueOf.apply(converted);
        } catch (IllegalArgumentException ignored) {
        }
        return value;
    }

    /** The reverse operation to {@link #asEnumValue(ModelNode, String, Function, Enum)}. */
    public static <E extends Enum<E>> String asAttributeValue(E enumValue) {
        String converted = enumValue.name().toLowerCase();
        return converted.replace('_', '-');
    }

    /**
     * Turns a list of properties into a model node.
     *
     * @param properties A list of properties with even size.
     * @return a model node with the specified properties.
     */
    public static ModelNode properties(String... properties) {
        ModelNode modelNode = new ModelNode();
        if (properties != null) {
            List<String> p = new ArrayList<>(asList(properties));
            for (Iterator<String> iterator = p.iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                if (iterator.hasNext()) {
                    String value = iterator.next();
                    if (value != null) {
                        modelNode.get(key).set(value);
                    }
                }
            }
        }
        return modelNode;
    }

    private ModelNodeHelper() {
    }
}
