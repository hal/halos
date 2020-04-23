package org.wildfly.halos.client.util;

import java.util.Arrays;

public class FormattingTuple {

    private String message;
    private Throwable throwable;
    private Object[] argArray;

    public FormattingTuple(String message) {
        this(message, (Object[]) null, (Throwable) null);
    }

    public FormattingTuple(String message, Object[] argArray, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
        if (throwable == null) {
            this.argArray = argArray;
        } else {
            this.argArray = trimmedCopy(argArray);
        }

    }

    static Object[] trimmedCopy(Object[] argArray) {
        if (argArray != null && argArray.length != 0) {
            return Arrays.copyOf(argArray, argArray.length - 1);
        } else {
            throw new IllegalStateException("non-sensical empty or null argument array");
        }
    }

    public String getMessage() {
        return this.message;
    }

    public Object[] getArgArray() {
        return this.argArray;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }
}