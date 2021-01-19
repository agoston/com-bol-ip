package com.bol.ipresource.etree;

import org.junit.Test;

public class ChildNodeTreeMapEmptyTest {

    @Test(expected = UnsupportedOperationException.class)
    public void removeChildIsUnsupported() {
        ChildNodeTreeMap.EMPTY.removeChild(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addChildIsUnsupported() {
        ChildNodeTreeMap.EMPTY.addChild(null);
    }
}
