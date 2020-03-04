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
package org.jboss.hal.core.mbui.dialog;

import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.wildfly.halos.console.resources.Names;

/**
 * A text box item useful for add resource dialogs. The form item has {@link ModelDescriptionConstants#NAME} as name,
 * is required and does not allow expressions.
 */
public class NameItem extends TextBoxItem {

    public NameItem() {
        super(ModelDescriptionConstants.NAME, Names.NAME);
        setRequired(true);
        setExpressionAllowed(false);
    }
}
