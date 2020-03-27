package org.wildfly.halos.console.util;

public final class Strings {

    public static String emptyToNull(String string) {
        return stringIsNullOrEmpty(string) ? null : string;
    }

    public static boolean stringIsNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String strip(String str, String delim) {
        str = stripStart(str, delim);
        return stripEnd(str, delim);
    }

    public static String stripStart(String str, String strip) {
        if (str == null) {
            return null;
        }

        int start = 0;
        int sz = str.length();
        if (strip == null) {
            while ((start != sz) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else {
            while ((start != sz) && (strip.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String stripEnd(String str, String strip) {
        if (str == null) {
            return null;
        }
        int end = str.length();

        if (strip == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else {
            while ((end != 0) && (strip.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    private Strings() {
    }
}
