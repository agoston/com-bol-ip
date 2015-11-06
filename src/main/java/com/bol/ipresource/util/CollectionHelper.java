package com.bol.ipresource.util;

import java.util.Collection;

public class CollectionHelper {
    public static <T> T uniqueResult(final Collection<T> c) {
        switch (c.size()) {
            case 0:
                return null;
            case 1:
                return c.iterator().next();
            default:
                throw new IllegalStateException("Unexpected number of elements in collection: " + c.size());
        }
    }
}
