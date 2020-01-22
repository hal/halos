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
package org.wildfly.halos.resources;

/**
 * IDs used in HTML elements and across multiple classes. Please add IDs to this interface even if there's already an
 * equivalent or similar constant in {@code ModelDescriptionConstants} (SoC).
 * <p>
 * The IDs defined here are reused by QA. So please make sure IDs are not spread over the code base but gathered in this
 * interface. This is not always possible - for instance if the ID contains dynamic parts like a resource name or
 * selected server. IDs which only contain static strings should be part of this interface.
 */
public interface Ids {

    String SERVER_ITEM = "server";
    String RESOURCES_ITEM = "resources";
    String MANAGEMENT_MODEL_ITEM = "management-model";
    String ROOT_CONTAINER = "halos-root";
}
