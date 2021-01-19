package com.bol.ipresource.ip;

import com.bol.ipresource.util.Validate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Efficient representation of an IPv4 address range. Internally IPv4 addresses
 * are stored as signed 32-bit <code>int</code>s. Externally they are
 * represented as <code>long</code>s to avoid issues with the sign-bit.
 */
public class Ipv4Interval extends IpInterval<Ipv4Interval> implements Comparable<Ipv4Interval> {
    public static final String IPV4_DOTLESS_REVERSE_DOMAIN = ".in-addr.arpa";
    public static final String IPV4_REVERSE_DOMAIN = ".in-addr.arpa.";

    private static final Splitter IPV4_TEXT_SPLITTER = Splitter.on('.');

    private static final long MINIMUM_NUMBER = 0;
    private static final long MAXIMUM_NUMBER = (1L << 32) - 1;

    /**
     * The IPv4 interval that includes all IPv4 addresses (0.0.0.0/0 in CIDR
     * notation).
     */
    public static final Ipv4Interval MAX_RANGE = new Ipv4Interval(MINIMUM_NUMBER, MAXIMUM_NUMBER);

    private static final Splitter SPLIT_ON_DOT = Splitter.on('.');
    private static final Pattern OCTET_PATTERN = Pattern.compile("^(?:[0-9]|[1-9][0-9]+)(?:-(?:[0-9]|[1-9][0-9]+)+)?$");

    private final int begin;
    private final int end;

    private Ipv4Interval(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Constructs a new IPv4 interval with the specified begin and end (both
     * inclusive).
     *
     * @param begin the first IPv4 address in this address range (inclusive).
     * @param end   the last IPv4 address in this address range (inclusive).
     * @throws IllegalArgumentException if the start or end addresses are invalid or if the start
     *                                  address is greater than the end address.
     */
    public Ipv4Interval(long begin, long end) {
        if (begin > end) {
            throw new IllegalArgumentException("Begin: " + begin + " not before End: " + end);
        }
        if (begin < MINIMUM_NUMBER) {
            throw new IllegalArgumentException("Begin: " + begin + " out of range");
        }
        if (end > MAXIMUM_NUMBER) {
            throw new IllegalArgumentException("End: " + end + " out of range");
        }

        this.begin = (int) begin;
        this.end = (int) end;
    }

    public static Ipv4Interval parse(InetAddress inetAddress) {
        if (!(inetAddress instanceof Inet4Address)) {
            throw new IllegalArgumentException("Not an IPv4 address: " + inetAddress);
        }
        byte[] addressArray = inetAddress.getAddress();
        int address = addressArray[3] & 0xFF;
        address |= ((addressArray[2] << 8) & 0xFF00);
        address |= ((addressArray[1] << 16) & 0xFF0000);
        address |= ((addressArray[0] << 24) & 0xFF000000);
        return new Ipv4Interval(address, address);
    }

    public static Ipv4Interval parse(String resource) {
        int indexOfSlash = resource.indexOf('/');
        if (indexOfSlash >= 0) {
            int begin = textToNumericFormat(resource.substring(0, indexOfSlash).trim());
            int prefixLength = -1;
            try {
                prefixLength = Integer.parseInt(resource.substring(indexOfSlash + 1).trim());
            } catch (NumberFormatException e) {}
            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("prefix length " + prefixLength + " is invalid");
            }
            int mask = (int) ((1L << (32 - prefixLength)) - 1);
            int end = begin | mask;
            begin = begin & ~mask;
            return new Ipv4Interval(begin, end);
        }

        int indexOfDash = resource.indexOf('-');
        if (indexOfDash >= 0) {
            long begin = ((long) textToNumericFormat(resource.substring(0, indexOfDash).trim())) & 0xffffffffL;
            long end = ((long) textToNumericFormat(resource.substring(indexOfDash + 1).trim())) & 0xffffffffL;
            return new Ipv4Interval(begin, end);
        }

        return parseIpAddress(resource);
    }

    public static Ipv4Interval parseIpAddress(String ipAddress) {
        int begin = textToNumericFormat(ipAddress.trim());
        return new Ipv4Interval(begin, begin);
    }

    public static Ipv4Interval parsePrefixWithLength(long prefix, int prefixLength) {
        long mask = (1L << (32 - prefixLength)) - 1;
        return new Ipv4Interval((prefix & ~mask) & 0xFFFFFFFFL, (prefix | mask) & 0xFFFFFFFFL);
    }

    static int reverseDomainIndex(String cleanAddress) {
        int index = cleanAddress.length() - IPV4_REVERSE_DOMAIN.length();
        if (cleanAddress.startsWith(IPV4_REVERSE_DOMAIN, index)) return index;

        index = cleanAddress.length() - IPV4_DOTLESS_REVERSE_DOMAIN.length();
        if (cleanAddress.startsWith(IPV4_DOTLESS_REVERSE_DOMAIN, index)) return index;

        return -1;
    }

    public static Ipv4Interval parseReverseDomain(String address) {
        Validate.notEmpty(address);
        String cleanAddress = address.trim().toLowerCase();

        int reverseDomainIndex = reverseDomainIndex(cleanAddress);
        Validate.isTrue(reverseDomainIndex >= 0, "Invalid reverse domain: ", address);

        return parseReverseDomain(cleanAddress, reverseDomainIndex);
    }

    static Ipv4Interval parseReverseDomain(String cleanAddress, int reverseDomainIndex) {
        cleanAddress = cleanAddress.substring(0, reverseDomainIndex);

        ArrayList<String> reverseParts = Lists.newArrayList(SPLIT_ON_DOT.split(cleanAddress));
        Validate.isTrue(!reverseParts.isEmpty() && reverseParts.size() <= 4, "Reverse address doesn't have between 1 and 4 octets: ", cleanAddress);

        List<String> parts = Lists.reverse(reverseParts);

        boolean hasDash = false;
        if (cleanAddress.contains("-")) {
            Validate.isTrue(reverseParts.get(0).contains("-"), "Dash notation not on last octet: ", cleanAddress);
            Validate.isTrue(cleanAddress.indexOf('-') == cleanAddress.lastIndexOf('-'), "Only one dash allowed: ", cleanAddress);
            hasDash = true;
        }

        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            Validate.isTrue(OCTET_PATTERN.matcher(part).matches(), "Invalid octet: ", part);
            // [EB]: Check for A-B && B <= A ?

            builder.append(part);
        }

        if (hasDash) {
            // [EB]: Some magic here, copy the 'start' of the string before the '-'
            // to get an expanded range: [1.1.1.]1-2 becomes 1.1.1.1-[1.1.1.]2
            int range = builder.indexOf("-");
            if (range != -1) {
                builder.insert(range + 1, builder.substring(0, builder.lastIndexOf(".") + 1));
            }
        }

        if (parts.size() < 4) {
            builder.append('/').append(parts.size() * 8);
        }

        return parse(builder.toString());
    }

    /**
     * @return the start address as "unsigned" <code>long</code>.
     */
    public long begin() {
        return ((long) begin) & 0xffffffffL;
    }

    /**
     * @return the end address as "unsigned" <code>long</code>.
     */
    public long end() {
        return ((long) end) & 0xffffffffL;
    }

    @Override
    public boolean contains(Ipv4Interval that) {
        return begin() <= that.begin() && end() >= that.end();
    }

    @Override
    public boolean intersects(Ipv4Interval that) {
        return (isIPWithinRange(begin(), that)
                || isIPWithinRange(end(), that)
                || isIPWithinRange(that.begin(), this));
    }

    private boolean isIPWithinRange(long ip, Ipv4Interval range) {
        return ip >= range.begin() && ip <= range.end();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + begin;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Ipv4Interval that = (Ipv4Interval) obj;
        return begin == that.begin && end == that.end;
    }

    /**
     * Only if x != 0
     */
    private static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    public static String numericToTextFormat(int src) {
        return (src >> 24 & 0xff) + "." + (src >> 16 & 0xff) + "." + (src >> 8 & 0xff) + "." + (src & 0xff);
    }

    public static int textToNumericFormat(String src) {
        int result = 0;
        Iterator<String> it = IPV4_TEXT_SPLITTER.split(src).iterator();
        for (int octet = 0; octet < 4; octet++) {
            result <<= 8;
            int value = -1;
            try {
                value = it.hasNext() ? Integer.parseInt(it.next()) : 0;
            } catch (NumberFormatException e) {}
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException(src + " is not a valid ipv4 address");
            }
            result |= value & 0xff;
        }
        if (it.hasNext()) {
            throw new IllegalArgumentException(src + " has more than 4 octets");
        }
        return result;
    }

    @Override
    public String toString() {
        int prefixLength = getPrefixLength();
        if (prefixLength < 0) {
            return toRangeString();
        } else {
            return numericToTextFormat(begin) + "/" + prefixLength;
        }
    }

    public String toRangeString() {
        return numericToTextFormat(begin) + " - " + numericToTextFormat(end);
    }

    public String toReverseDomain() {
        int prefixLength = getPrefixLength();
        if (prefixLength < 0) throw new IllegalArgumentException("Ipv4Interval " + toRangeString() + " is not a prefix");

        byte[] b = Ints.toByteArray(begin);
        byte[] e = Ints.toByteArray(end);

        StringBuilder sb = new StringBuilder();
        // if prefixlength == 0, -1 >> 3 = -1, so this will not run
        for (int nibble = (prefixLength - 1) >> 3; nibble >= 0; nibble--) {
            // ' & 0xff' is used to convert unsigned byte to signed int... :facepalm:
            if (b[nibble] == e[nibble]) sb.append(b[nibble] & 0xff);
            else sb.append(b[nibble] & 0xff).append('-').append(e[nibble] & 0xff);

            if (nibble > 0) sb.append('.');
        }

        sb.append(IPV4_REVERSE_DOMAIN);

        return sb.toString();
    }

    public String beginAddressAsString() {
        return numericToTextFormat(begin);
    }

    public String endAddressAsString() {
        return numericToTextFormat(end);
    }

    /**
     * Orders on {@link #begin} ASCENDING and {@link #end} DESCENDING. This puts
     * less-specific ranges before more-specific ranges.
     */
    @Override
    public int compareTo(Ipv4Interval that) {
        if (begin() < that.begin()) {
            return -1;
        } else if (begin() > that.begin()) {
            return 1;
        } else if (that.end() < end()) {
            return -1;
        } else if (that.end() > end()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Ipv4Interval singletonIntervalAtLowerBound() {
        return new Ipv4Interval(begin(), begin());
    }

    @Override
    public int compareUpperBound(Ipv4Interval that) {
        long thisEnd = end();
        long thatEnd = that.end();
        return thisEnd < thatEnd ? -1 : thisEnd > thatEnd ? 1 : 0;
    }

    @Override
    public InetAddress beginAsInetAddress() {
        return InetAddresses.fromInteger(begin);
    }

    @Override
    public InetAddress endAsInetAddress() {
        return InetAddresses.fromInteger(end);
    }

    @Override
    public byte[] beginAsByteArray() {
        return Ints.toByteArray(begin);
    }

    @Override
    public byte[] endAsByteArray() {
        return Ints.toByteArray(end);
    }

    @Override
    public int getPrefixLength() {
        // see if we can convert to nice prefix
        if (isPowerOfTwo(end - begin + 1)) {
            return 32 - Integer.numberOfTrailingZeros(end - begin + 1);
        } else {
            return -1;
        }
    }
}
