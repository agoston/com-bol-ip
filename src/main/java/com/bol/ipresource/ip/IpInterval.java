package com.bol.ipresource.ip;

import java.net.Inet4Address;
import java.net.InetAddress;

public abstract class IpInterval<K extends Interval<K>> implements Interval<K> {
    public static String removeTrailingDot(String address) {
        if (address.endsWith(".")) {
            return address.substring(0, address.length() - 1);
        }

        return address;
    }

    public static IpInterval<?> parse(String addressPrefixOrRange) {
        if (addressPrefixOrRange.indexOf(':') == -1) {
            return Ipv4Resource.parse(addressPrefixOrRange);
        }

        return Ipv6Resource.parse(addressPrefixOrRange);
    }

    public static IpInterval<?> parseReverseDomain(String reverse) {
        String result = removeTrailingDot(reverse).toLowerCase();

        if (result.endsWith(Ipv4Resource.IPV4_REVERSE_DOMAIN)) {
            return Ipv4Resource.parseReverseDomain(result);
        }

        return Ipv6Resource.parseReverseDomain(result);
    }

    public static IpInterval<?> asIpInterval(InetAddress address) {
        if (address instanceof Inet4Address) {
            return Ipv4Resource.parse(address);
        }

        return Ipv6Resource.parse(address);
    }

    public abstract InetAddress beginAsInetAddress();

    public abstract byte[] beginAsByteArray();

    public abstract int getPrefixLength();
}
