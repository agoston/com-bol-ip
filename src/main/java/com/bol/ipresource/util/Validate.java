package com.bol.ipresource.util;

import java.util.Collection;

public class Validate {
    public static void notNull(Object object) {
        notNull(object, "The validated object is null");
    }

    public static void notNull(Object object, String message) {
        if (object == null) throw new IllegalArgumentException(message);
    }

    public static void notEmpty(Collection collection, String message) {
        if (collection == null || collection.size() == 0) throw new IllegalArgumentException(message);
    }

    public static void notEmpty(Collection collection) {
        notEmpty(collection, "The validated collection is empty");
    }

    public static void notEmpty(String string, String message) {
        if (string == null || string.length() == 0) throw new IllegalArgumentException(message);
    }

    public static void notEmpty(String string) {
        notEmpty(string, "The validated string is empty");
    }

    public static void isTrue(boolean expression, String message, Object value) {
        if (!expression) throw new IllegalArgumentException(message + value);
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }
}
