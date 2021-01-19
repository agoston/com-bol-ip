package com.bol.ipresource.ip;

import com.bol.ipresource.etree.IntervalMap;
import com.bol.ipresource.etree.NestedIntervalMap;

import java.util.List;

/**
 * Handy tiny little wrapper around {@link com.bol.ipresource.etree.NestedIntervalMap} to allow for a protocol-independent tree.
 *
 * Tests are omitted as this merely acts as a logic-less wrapper.
 */
public class IpTree<V> implements IntervalMap<IpInterval<?>, V> {
    protected NestedIntervalMap<Ipv4Interval, V> ipv4Tree = new NestedIntervalMap<>();
    protected NestedIntervalMap<Ipv6Interval, V> ipv6Tree = new NestedIntervalMap<>();

    @Override
    public void put(IpInterval<?> key, V value) {
        if (key.getClass() == Ipv4Interval.class) ipv4Tree.put((Ipv4Interval) key, value);
        else ipv6Tree.put((Ipv6Interval) key, value);
    }

    @Override
    public void remove(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) ipv4Tree.remove((Ipv4Interval) key);
        else ipv6Tree.remove((Ipv6Interval) key);
    }

    @Override
    public void remove(IpInterval<?> key, V value) {
        if (key.getClass() == Ipv4Interval.class) ipv4Tree.remove((Ipv4Interval) key, value);
        else ipv6Tree.remove((Ipv6Interval) key, value);
    }

    @Override
    public void clear() {
        ipv4Tree.clear();
        ipv6Tree.clear();
    }

    @Override
    public List<V> findFirstLessSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findFirstLessSpecific((Ipv4Interval) key);
        else return ipv6Tree.findFirstLessSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findExact(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findExact((Ipv4Interval) key);
        else return ipv6Tree.findExact((Ipv6Interval) key);
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findExactOrFirstLessSpecific((Ipv4Interval) key);
        else return ipv6Tree.findExactOrFirstLessSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findAllLessSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findAllLessSpecific((Ipv4Interval) key);
        else return ipv6Tree.findAllLessSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findExactAndAllLessSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findExactAndAllLessSpecific((Ipv4Interval) key);
        else return ipv6Tree.findExactAndAllLessSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findFirstMoreSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findFirstMoreSpecific((Ipv4Interval) key);
        else return ipv6Tree.findFirstMoreSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findAllMoreSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findAllMoreSpecific((Ipv4Interval) key);
        else return ipv6Tree.findAllMoreSpecific((Ipv6Interval) key);
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(IpInterval<?> key) {
        if (key.getClass() == Ipv4Interval.class) return ipv4Tree.findExactAndAllMoreSpecific((Ipv4Interval) key);
        else return ipv6Tree.findExactAndAllMoreSpecific((Ipv6Interval) key);
    }

    @Override
    public String toString() {
        return ipv4Tree.toString() + ", " + ipv6Tree.toString();
    }
}
