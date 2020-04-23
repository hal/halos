package org.wildfly.halos.client.util;

import static elemental2.dom.DomGlobal.console;

public class Logger {

    public static void info(String message, Object... args) {
        console.info(format(message, args));
    }

    public static void error(String message, Object... args) {
        console.error(format(message, args));
    }

    public static void warn(String message, Object... args) {
        console.warn(format(message, args));
    }

    public static void debug(String message, Object... args) {
        console.debug(format(message, args));
    }

    private static String format(String message, Object... args) {
        if (message == null) {
            return "";
        } else if (args == null) {
            return message;
        } else {
            StringBuilder builder = new StringBuilder(message.length() + 50);
            int pos = 0;

            int i;
            for (i = 0; i < args.length; ++i) {
                int indexOf = message.indexOf("{}", pos);
                if (indexOf == -1) {
                    if (pos == 0) {
                        return message;
                    }
                    builder.append(message.substring(pos));
                    return builder.toString();
                }

                if (isEscapedDelimeter(message, indexOf)) {
                    if (!isDoubleEscaped(message, indexOf)) {
                        --i;
                        builder.append(message, pos, indexOf - 1);
                        builder.append('{');
                        pos = indexOf + 1;
                    } else {
                        builder.append(message, pos, indexOf - 1);
                        builder.append(args[i]);
                        pos = indexOf + 2;
                    }
                } else {
                    builder.append(message, pos, indexOf);
                    builder.append(args[i]);
                    pos = indexOf + 2;
                }
            }
            builder.append(message.substring(pos));
            return builder.toString();
        }
    }

    private static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {
        if (delimeterStartIndex == 0) {
            return false;
        } else {
            char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
            return potentialEscape == '\\';
        }
    }

    private static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        return delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == '\\';
    }
}
