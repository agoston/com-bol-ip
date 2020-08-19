package com.bol.ipresource.etree;

import com.bol.ipresource.ip.Ipv4Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InternalNodeTest {

    private InternalNode<Ipv4Interval, String> a = new InternalNode<>(new Ipv4Interval(1, 2), "1-2");
    private InternalNode<Ipv4Interval, String> b = new InternalNode<>(new Ipv4Interval(1, 2), "1-2");
    private InternalNode<Ipv4Interval, String> c = new InternalNode<>(new Ipv4Interval(1, 4), "1-4");
    private InternalNode<Ipv4Interval, String> d = new InternalNode<>(new Ipv4Interval(1, 4), "1-4");
    private InternalNode<Ipv4Interval, String> e = new InternalNode<>(new Ipv4Interval(2, 5), "2-5");

    @Before
    public void setup() {
        d.addChild(a);
    }

    @Test
    public void test_equals_and_hashcode() {
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(c, c);
        assertFalse(a.equals(c));
        assertFalse(c.equals(a));
        assertFalse(c.equals(d));

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == c.hashCode());
        assertFalse(c.hashCode() == d.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_intersect_insert_fails() {
        c.addChild(e);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_intersect_remove_fails() {
        c.removeChild(e.getInterval());
    }

}
