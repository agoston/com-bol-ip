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
            return Ipv4Interval.parse(addressPrefixOrRange);
        }

        return Ipv6Interval.parse(addressPrefixOrRange);
    }

    public static IpInterval<?> parseReverseDomain(String reverse) {
        String result = removeTrailingDot(reverse).toLowerCase();

        if (result.endsWith(Ipv4Interval.IPV4_REVERSE_DOMAIN)) {
            return Ipv4Interval.parseReverseDomain(result);
        }

        return Ipv6Interval.parseReverseDomain(result);
    }

    public static IpInterval<?> asIpInterval(InetAddress address) {
        if (address instanceof Inet4Address) {
            return Ipv4Interval.parse(address);
        }

        return Ipv6Interval.parse(address);
    }

    public abstract String toRangeString();

    public abstract String beginAddressAsString();
    public abstract String endAddressAsString();

    public abstract InetAddress beginAsInetAddress();
    public abstract InetAddress endAsInetAddress();

    public abstract byte[] beginAsByteArray();
    public abstract byte[] endAsByteArray();

    public abstract int getPrefixLength();
}
