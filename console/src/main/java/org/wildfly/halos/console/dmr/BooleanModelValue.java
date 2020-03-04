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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class BooleanModelValue extends ModelValue {

    private static BooleanModelValue TRUE = new BooleanModelValue(true);
    private static BooleanModelValue FALSE = new BooleanModelValue(false);

    static BooleanModelValue valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    private final boolean value;

    private BooleanModelValue(boolean value) {
        super(ModelType.BOOLEAN);
        this.value = value;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeBoolean(value);
    }

    @Override
    long asLong() {
        return value ? 1 : 0;
    }

    @Override
    long asLong(long defVal) {
        return value ? 1 : 0;
    }

    @Override
    int asInt() {
        return value ? 1 : 0;
    }

    @Override
    int asInt(int defVal) {
        return value ? 1 : 0;
    }

    @Override
    boolean asBoolean() {
        return value;
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return value;
    }

    @Override
    double asDouble() {
        return value ? 1.0 : 0.0;
    }

    @Override
    double asDouble(double defVal) {
        return value ? 1.0 : 0.0;
    }

    @Override
    byte[] asBytes() {
        return value ? new byte[]{1} : new byte[]{0};
    }

    @Override
    BigDecimal asBigDecimal() {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    BigInteger asBigInteger() {
        return value ? BigInteger.ONE : BigInteger.ZERO;
    }

    @Override
    String asString() {
        return Boolean.toString(value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this;
    }

    @Override
    public int hashCode() {
        return Boolean.valueOf(value).hashCode();
    }
}
