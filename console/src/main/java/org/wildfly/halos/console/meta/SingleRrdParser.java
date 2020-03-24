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

import java.util.List;

import org.wildfly.halos.console.dmr.ModelNode;
import org.wildfly.halos.console.dmr.ModelType;
import org.wildfly.halos.console.dmr.Property;
import org.wildfly.halos.console.dmr.ResourceAddress;
import org.wildfly.halos.console.meta.description.ResourceDescription;
import org.wildfly.halos.console.meta.security.SecurityContext;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.*;

/**
 * This class does the bulk of work when it comes to parse the {@code read-resource-description} response and collect
 * the results.
 */
class SingleRrdParser {

    private final RrdResult rrdResult;

    SingleRrdParser(RrdResult rrdResult) {
        this.rrdResult = rrdResult;
    }

    RrdResult parse(ResourceAddress address, ModelNode modelNode, boolean recursive) {
        if (modelNode.getType() == ModelType.LIST) {
            for (ModelNode nestedNode : modelNode.asList()) {
                ResourceAddress nestedAddress = new ResourceAddress(nestedNode.get(ADDRESS));
                ModelNode nestedResult = nestedNode.get(RESULT);
                parseSingle(nestedAddress, nestedResult, recursive);
            }
        } else {
            parseSingle(address, modelNode, recursive);
        }
        return rrdResult;
    }

    private void parseSingle(ResourceAddress address, ModelNode modelNode, boolean recursive) {
        // resource description
        if (!rrdResult.containsResourceDescription(address) && modelNode.hasDefined(DESCRIPTION)) {
            ResourceDescription resourceDescription = new ResourceDescription(modelNode, recursive);
            rrdResult.addResourceDescription(resourceDescriptionAddress(address), resourceDescription);
        }

        // security context
        ModelNode accessControl = modelNode.get(ACCESS_CONTROL);
        if (accessControl.isDefined()) {
            if (!rrdResult.containsSecurityContext(address) && accessControl.hasDefined(DEFAULT)) {
                SecurityContext securityContext = new SecurityContext(accessControl.get(DEFAULT), recursive);
                rrdResult.addSecurityContext(address, securityContext);
            }

            // exceptions
            if (accessControl.hasDefined(EXCEPTIONS)) {
                List<Property> exceptions = accessControl.get(EXCEPTIONS).asPropertyList();
                for (Property property : exceptions) {
                    ModelNode exception = property.getValue();
                    ResourceAddress exceptionAddress = new ResourceAddress(exception.get(ADDRESS));
                    if (!rrdResult.containsSecurityContext(exceptionAddress)) {
                        // only the top-level result gets the recursive flag
                        rrdResult.addSecurityContext(exceptionAddress, new SecurityContext(exception, false));
                    }
                }
            }
        }

        // to reduce the payload we only use the flat model node w/o children
        ModelNode childrenNode = modelNode.hasDefined(CHILDREN) ? modelNode.remove(CHILDREN) : new ModelNode();
        if (childrenNode.isDefined()) {
            List<Property> children = childrenNode.asPropertyList();
            for (Property child : children) {
                String addressKey = child.getName();
                if (child.getValue().hasDefined(MODEL_DESCRIPTION)) {
                    List<Property> modelDescriptions = child.getValue().get(MODEL_DESCRIPTION).asPropertyList();
                    for (Property modelDescription : modelDescriptions) {
                        String addressValue = modelDescription.getName();
                        ModelNode childNode = modelDescription.getValue();
                        ResourceAddress childAddress = new ResourceAddress(address).add(addressKey, addressValue);
                        // only the top-level result gets the recursive flag
                        parseSingle(childAddress, childNode, false);
                    }
                }
            }
        }
    }

    private ResourceAddress resourceDescriptionAddress(ResourceAddress address) {
        // TODO adjust resource address for resource descriptions
        return address;
    }
}
