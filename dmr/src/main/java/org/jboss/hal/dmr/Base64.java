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
package org.jboss.hal.dmr;

import jsinterop.annotations.JsMethod;

import static jsinterop.annotations.JsPackage.GLOBAL;

/** Encodes and decodes to and from Base64 notation. */
public class Base64 {

    @JsMethod(namespace = GLOBAL, name = "btoa")
    public static native String encode(String decoded);

    @JsMethod(namespace = GLOBAL, name = "atob")
    public static native String decode(String encoded);

    /** Defeats instantiation. */
    private Base64() {
    }
}