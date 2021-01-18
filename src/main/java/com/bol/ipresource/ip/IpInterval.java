package com.bol.ipresource.ip;

import com.bol.ipresource.util.Validate;

import java.net.Inet4Address;
import java.net.InetAddress;

public abstract class IpInterval<K> implements Interval<K> {
    public static IpInterval<?> parse(String addressPrefixOrRange) {
        if (addressPrefixOrRange.indexOf(':') == -1) {
            return Ipv4Interval.parse(addressPrefixOrRange);
        }

        return Ipv6Interval.parse(addressPrefixOrRange);
    }

    public static IpInterval<?> parseAddress(String address) {
        if (address.indexOf(':') == -1) {
            return Ipv4Interval.parseAddress(address);
        }

        return Ipv6Interval.parseAddress(address);
    }

    public static IpInterval<?> parseReverseDomain(String reverse) {
        Validate.notEmpty(reverse);
        String cleanAddress = reverse.trim().toLowerCase();

        int reverseDomainIndex = Ipv4Interval.reverseDomainIndex(cleanAddress);
        if (reverseDomainIndex >= 0) return Ipv4Interval.parseReverseDomain(cleanAddress, reverseDomainIndex);

        reverseDomainIndex = Ipv6Interval.reverseDomainIndex(cleanAddress);
        if (reverseDomainIndex >= 0) return Ipv6Interval.parseReverseDomain(cleanAddress, reverseDomainIndex);

        throw new IllegalArgumentException("Invalid reverse domain: " + cleanAddress);
    }

    public static IpInterval<?> asIpInterval(InetAddress address) {
        if (address instanceof Inet4Address) {
            return Ipv4Interval.parse(address);
        }

        return Ipv6Interval.parse(address);
    }

    /**
     * @returns a fully qualifies reverse domain, with trailing dot, e.g. 66.152.in-addr.arpa.
     */
    public abstract String toReverseDomain();

    /**
     * @returns same as `toString()`, but forced into range format ("X - Y")
     */
    public abstract String toRangeString();

    /**
     * @returns `toString()` on the begin address of this interval
     */
    public abstract String beginAddressAsString();

    /**
     * @returns `toString()` on the end address of this interval
     */
    public abstract String endAddressAsString();

    public abstract InetAddress beginAsInetAddress();

    public abstract InetAddress endAsInetAddress();

    public abstract byte[] beginAsByteArray();

    public abstract byte[] endAsByteArray();

    public abstract int getPrefixLength();
}
