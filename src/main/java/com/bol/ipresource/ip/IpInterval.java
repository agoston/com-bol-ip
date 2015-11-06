package com.bol.ipresource.ip;

import java.net.Inet4Address;
import java.net.InetAddress;

public abstract class IpInterval<K extends Interval<K>> implements Interval<K> {
    public static String removeTrailingDot(final String address) {
        if (address.endsWith(".")) {
            return address.substring(0, address.length() - 1);
        }

        return address;
    }

    public static IpInterval<?> parse(final String prefix) {
        if (prefix.indexOf(':') == -1) {
            return Ipv4Resource.parse(prefix);
        }

        return Ipv6Resource.parse(prefix);
    }

    public static IpInterval<?> parseReverseDomain(final String reverse) {
        final String result = removeTrailingDot(reverse).toLowerCase();

        if (result.endsWith(Ipv4Resource.IPV4_REVERSE_DOMAIN)) {
            return Ipv4Resource.parseReverseDomain(result);
        }

        return Ipv6Resource.parseReverseDomain(result);
    }

    public static IpInterval<?> asIpInterval(final InetAddress address) {
        if (address instanceof Inet4Address) {
            return new Ipv4Resource(address);
        }

        return Ipv6Resource.parse(address);
    }

    public abstract InetAddress beginAsInetAddress();

    public abstract int getPrefixLength();
}
