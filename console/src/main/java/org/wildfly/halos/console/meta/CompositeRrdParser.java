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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.dmr.CompositeResult;
import org.wildfly.halos.console.dmr.ModelNode;
import org.wildfly.halos.console.dmr.ModelType;
import org.wildfly.halos.console.dmr.Operation;
import org.wildfly.halos.console.dmr.Property;
import org.wildfly.halos.console.dmr.ResourceAddress;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.ADDRESS;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.HOST;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.RESULT;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.SERVER;

class CompositeRrdParser {

    private static final Logger logger = LoggerFactory.getLogger(CompositeRrdParser.class);

    private final boolean recursive;

    public CompositeRrdParser(boolean recursive) {
        this.recursive = recursive;
    }

    RrdResult parse(CompositeResult compositeResult) {
        int index = 0;
        RrdResult rrdResult = new RrdResult();

        for (ModelNode step : compositeResult) {
            if (step.isFailure()) {
                throw new MetadataException("Failed step 'step-" + (index + 1) + "' in composite rrd result: " +
                        step.getFailureDescription());
            }

            ModelNode stepResult = step.get(RESULT);

            if (stepResult.getType() == ModelType.LIST) {
                // multiple rrd results each with its own address
                for (ModelNode modelNode : stepResult.asList()) {
                    ModelNode result = modelNode.get(RESULT);
                    if (result.isDefined()) {
                        ResourceAddress operationAddress = operationAddress(compositeResult, index);
                        ResourceAddress resultAddress = new ResourceAddress(modelNode.get(ADDRESS));
                        ResourceAddress resolvedAddress = makeFqAddress(operationAddress, resultAddress);

                        new SingleRrdParser(rrdResult, recursive).parse(resolvedAddress, result);
                    }
                }

            } else {
                // a single rrd result
                ResourceAddress address = operationAddress(compositeResult, index);
                new SingleRrdParser(rrdResult, recursive).parse(address, stepResult);
            }
            index++;
        }

        return rrdResult;
    }

    private ResourceAddress operationAddress(CompositeResult compositeResult, int index) {
        Operation operation = compositeResult.operation(index);
        if (operation == null) {
            throw new MetadataException("Cannot get operation at index " + index);
        }
        return operation.address;
    }

    private ResourceAddress makeFqAddress(ResourceAddress operationAddress, ResourceAddress resultAddress) {
        ResourceAddress resolved = resultAddress;
        List<Property> operationSegments = operationAddress.asPropertyList();
        List<Property> resultSegments = resultAddress.asPropertyList();

        // For rrd operations against running servers using wildcards like /host=master/server=server-one/interface=*
        // the result does *not* contain absolute addresses. Since we need them in the registries,
        // this method fixes this corner case.
        if (operationSegments.size() > 2 &&
                operationSegments.size() == resultSegments.size() + 2 &&
                HOST.equals(operationSegments.get(0).getName()) &&
                SERVER.equals(operationSegments.get(1).getName())) {
            resolved = new ResourceAddress()
                    .add(HOST, operationSegments.get(0).getValue().asString())
                    .add(SERVER, operationSegments.get(1).getValue().asString())
                    .add(resultAddress);
            logger.debug("Adjust result address '{}' -> '{}'", resultAddress, resolved);
        }
        return resolved;
    }
}
