package com.bol.ipresource.ip;

import com.bol.ipresource.util.Validate;
import com.google.common.net.InetAddresses;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Efficient representation of an IPv6 address range. Internally IPv6 addresses are stored as 2 signed 64-bit <code>long</code>s.
 * <p>
 * Note that while ipv6 addresses are always prefixes, this class also allows for ranges that cannot be represented as prefix.
 */
public class Ipv6Interval extends IpInterval<Ipv6Interval> implements Comparable<Ipv6Interval> {
    public static final String IPV6_DOTLESS_REVERSE_DOMAIN = ".ip6.arpa";
    public static final String IPV6_REVERSE_DOMAIN = ".ip6.arpa.";
    private static final Pattern REVERSE_PATTERN = Pattern.compile("(?i)^[0-9a-f](?:[.][0-9a-f]){0,31}$");
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final int LONG_BITCOUNT = 64;
    private static final int IPV6_BITCOUNT = 128;

    public static final Ipv6Interval MAX_RANGE = new Ipv6Interval(0, 0, 0);
    private static final BigInteger MASK = BigInteger.ONE.shiftLeft(LONG_BITCOUNT).subtract(BigInteger.ONE);

    private final long beginMsb;
    private final long beginLsb;
    private final long endMsb;
    private final long endLsb;

    public Ipv6Interval(BigInteger address, int prefixLength) {
        this(msb(address), lsb(address), prefixLength);
    }

    public Ipv6Interval(String msb, String lsb, int prefixLength) {
        this(new BigInteger(msb).shiftLeft(LONG_BITCOUNT).add(new BigInteger(lsb)), prefixLength);
    }

    public Ipv6Interval(long msb, long lsb, int prefixLength) {
        // Special cases -- short circuit
        if (prefixLength == 0) {
            beginMsb = 0;
            beginLsb = 0;
            endMsb = ~0;
            endLsb = ~0;
            return;
        } else if (prefixLength == LONG_BITCOUNT) {
            beginMsb = msb;
            beginLsb = 0;
            endMsb = msb;
            endLsb = ~0;
            return;
        } else if (prefixLength == IPV6_BITCOUNT) {
            beginMsb = msb;
            beginLsb = lsb;
            endMsb = msb;
            endLsb = lsb;
            return;
        }

        long mask = (1L << (LONG_BITCOUNT - (prefixLength % LONG_BITCOUNT))) - 1;

        if (prefixLength < LONG_BITCOUNT) {
            beginLsb = 0;
            endLsb = ~0;

            beginMsb = msb & ~mask;
            endMsb = msb | mask;
        } else {
            beginMsb = msb;
            endMsb = msb;

            beginLsb = lsb & ~mask;
            endLsb = lsb | mask;
        }
    }

    public Ipv6Interval(BigInteger begin, BigInteger end) {
        Validate.isTrue(begin.bitLength() <= IPV6_BITCOUNT, "Begin out of range: ", begin);
        Validate.isTrue(end.bitLength() <= IPV6_BITCOUNT, "End out of range: ", end);

        beginMsb = msb(begin);
        beginLsb = lsb(begin);
        endMsb = msb(end);
        endLsb = lsb(end);

        Validate.isTrue(compare(beginMsb, beginLsb, endMsb, endLsb) <= 0, "Begin must be before end");
    }

    public Ipv6Interval(long beginMsb, long beginLsb, long endMsb, long endLsb) {
        this.beginMsb = beginMsb;
        this.beginLsb = beginLsb;
        this.endMsb = endMsb;
        this.endLsb = endLsb;

        Validate.isTrue(compare(beginMsb, beginLsb, endMsb, endLsb) <= 0, "Begin must be before end");
    }

    public static Ipv6Interval parse(InetAddress ipv6Address) {
        return parse(ipv6Address, IPV6_BITCOUNT);
    }

    public static Ipv6Interval parse(InetAddress ipv6Address, int prefixLength) {
        long[] res = byteArrayToLongArray(ipv6Address.getAddress());
        return new Ipv6Interval(res[0], res[1], prefixLength);
    }

    public static Ipv6Interval parse(String prefixOrAddress) {
        String trimmedPrefixOrAddress = prefixOrAddress.trim();
        int slashIndex = trimmedPrefixOrAddress.indexOf('/');

        if (slashIndex > 0) {
            int prefixLength = -1;
            try {
                prefixLength = Integer.parseInt(trimmedPrefixOrAddress.substring(slashIndex + 1));
            } catch (NumberFormatException e) {}
            if (prefixLength < 0 || prefixLength > 128) {
                throw new IllegalArgumentException("Invalid prefix length: " + prefixOrAddress);
            }
            return parse(InetAddresses.forString(trimmedPrefixOrAddress.substring(0, slashIndex)), prefixLength);
        } else {
            return parse(InetAddresses.forString(trimmedPrefixOrAddress), IPV6_BITCOUNT);
        }
    }

    static int reverseDomainIndex(String cleanAddress) {
        int withDotIndex = cleanAddress.length() - IPV6_REVERSE_DOMAIN.length();
        if (cleanAddress.startsWith(IPV6_REVERSE_DOMAIN, withDotIndex)) return withDotIndex;

        int withoutDotIndex = cleanAddress.length() - IPV6_DOTLESS_REVERSE_DOMAIN.length();
        if (cleanAddress.startsWith(IPV6_DOTLESS_REVERSE_DOMAIN, withDotIndex)) return withoutDotIndex;

        return -1;
    }

    public static Ipv6Interval parseReverseDomain(String address) {
        Validate.notEmpty(address);
        String cleanAddress = address.trim().toLowerCase();

        int reverseDomainIndex = reverseDomainIndex(cleanAddress);
        Validate.isTrue(reverseDomainIndex >= 0, "Invalid reverse domain: ", address);

        return parseReverseDomain(cleanAddress, reverseDomainIndex);
    }

    static Ipv6Interval parseReverseDomain(String cleanAddress, int reverseDomainIndex) {
        cleanAddress = cleanAddress.substring(0, reverseDomainIndex);

        // FIXME: this translates reverse domain address into a forward address; write a proper parser for performance
        Validate.isTrue(REVERSE_PATTERN.matcher(cleanAddress).matches(), "Invalid reverse domain: ", cleanAddress);

        StringBuilder builder = new StringBuilder();
        int netmask = 0;

        for (int index = cleanAddress.length() - 1; index >= 0; index -= 2) {
            builder.append(cleanAddress.charAt(index));

            netmask += 4;

            if (netmask % 16 == 0 && index > 0) {
                builder.append(':');
            }
        }

        if (netmask % 16 != 0) {
            for (int i = 4 - ((netmask / 4) % 4); i > 0; i--) {
                builder.append('0');
            }
        }

        if (netmask <= 112) {
            builder.append("::");
        }

        builder.append('/');
        builder.append(netmask);

        return parse(builder.toString());
    }

    public BigInteger beginAsBigInteger() {
        return twoUnsignedLongToBigInteger(beginMsb, beginLsb);
    }

    public BigInteger endAsBigInteger() {
        return twoUnsignedLongToBigInteger(endMsb, endLsb);
    }

    private static long[] byteArrayToLongArray(byte[] address) {
        Validate.isTrue(address.length == 16, "Address has to be 16 bytes long");
        long[] res = new long[2];

        for (int i = 0; i < 16; i++) {
            res[i >>> 3] = (res[i >>> 3] << 8) + (address[i] & 0xFF);
        }
        return res;
    }

    private static byte[] toByteArray(long msb, long lsb) {
        byte[] data = new byte[16];

        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (msb & 0xffL);
            msb >>= 8;
        }
        for (int i = 15; i >= 8; i--) {
            data[i] = (byte) (lsb & 0xffL);
            lsb >>= 8;
        }

        return data;
    }

    private static BigInteger twoUnsignedLongToBigInteger(long msb, long lsb) {
        return new BigInteger(1, toByteArray(msb, lsb));
    }

    public static int compare(long aMsb, long aLsb, long bMsb, long bLsb) {
        if (aMsb == bMsb) {
            if (aLsb == bLsb) {
                return 0;
            }
            if ((aLsb < bLsb) ^ (aLsb < 0) ^ (bLsb < 0)) {
                return -1;
            }
        } else if ((aMsb < bMsb) ^ (aMsb < 0) ^ (bMsb < 0)) {
            return -1;
        }

        return 1;
    }

    @Override
    public int compareTo(Ipv6Interval that) {
        int comp = compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb);
        if (comp == 0) {
            comp = compare(that.endMsb, that.endLsb, endMsb, endLsb);
        }
        return comp;
    }

    @Override
    public boolean contains(Ipv6Interval that) {
        return compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb) <= 0
                && compare(endMsb, endLsb, that.endMsb, that.endLsb) >= 0;
    }

    @Override
    public boolean intersects(Ipv6Interval that) {
        return (compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb) >= 0 && compare(beginMsb, beginLsb, that.endMsb, that.endLsb) <= 0)
                || (compare(endMsb, endLsb, that.beginMsb, that.beginLsb) >= 0 && compare(endMsb, endLsb, that.endMsb, that.endLsb) <= 0)
                || contains(that);
    }

    @Override
    public Ipv6Interval singletonIntervalAtLowerBound() {
        return new Ipv6Interval(beginMsb, beginLsb, IPV6_BITCOUNT);
    }

    @Override
    public int compareUpperBound(Ipv6Interval that) {
        return compare(endMsb, endLsb, that.endMsb, that.endLsb);
    }

    @Override
    public InetAddress beginAsInetAddress() {
        try {
            return Inet6Address.getByAddress(toByteArray(beginMsb, beginLsb));
        } catch (UnknownHostException e) {
            // this will never happen
            return null;
        }
    }

    @Override
    public InetAddress endAsInetAddress() {
        try {
            return Inet6Address.getByAddress(toByteArray(endMsb, endLsb));
        } catch (UnknownHostException e) {
            // this will never happen
            return null;
        }
    }

    @Override
    public byte[] beginAsByteArray() {
        return toByteArray(beginMsb, beginLsb);
    }

    @Override
    public byte[] endAsByteArray() {
        return toByteArray(endMsb, endLsb);
    }

    @Override
    public int getPrefixLength() {
        int res;
        if (beginMsb == endMsb) {
            res = LONG_BITCOUNT + Long.bitCount(~(beginLsb ^ endLsb));
        } else {
            res = Long.bitCount(~(beginMsb ^ endMsb));
        }
        return res;
    }

    private static void numericToTextFormat(StringBuilder sb, long msb, long lsb, int prefixLength) {
        int[] nibbles = new int[8];
        int maxZeroIndex = -1, maxZeroCount = -1;
        int actZeroIndex = -1, actZeroCount = 0;

        // convert to nibbles, mark location of longest nibble
        for (int i = 0; i < prefixLength; i += 16) {
            long act = (i < LONG_BITCOUNT) ? msb : lsb;
            int remainingPrefix = prefixLength - i;
            int mask = 0xFFFF;

            if (remainingPrefix < 16) {
                mask &= ~((1 << (16 - remainingPrefix)) - 1);
            }

            nibbles[i >>> 4] = (int) (act >> (48 - (i & 63))) & mask;
        }

        // look for longest nibble location
        for (int i = 0; i < 8; i++) {
            if (nibbles[i] == 0) {
                if (actZeroIndex >= 0) {
                    actZeroCount++;
                } else {
                    actZeroIndex = i;
                    actZeroCount = 1;
                }
            } else {
                if (actZeroIndex >= 0) {
                    if (actZeroCount >= maxZeroCount) {
                        maxZeroCount = actZeroCount;
                        maxZeroIndex = actZeroIndex;
                    }
                }
                actZeroIndex = -1;
            }
        }

        if ((actZeroIndex >= 0) && (actZeroCount >= maxZeroCount)) {
            maxZeroCount = actZeroCount;
            maxZeroIndex = actZeroIndex;
        }

        // convert to string
        for (int i = 0; i < 8; i++) {
            if (maxZeroIndex == i) {
                if (i == 0) {
                    sb.append("::");
                } else {
                    sb.append(':');
                }
                i += maxZeroCount - 1;
            } else {
                sb.append(Integer.toHexString(nibbles[i]));
                if (i < 7) {
                    sb.append(':');
                }
            }
        }
    }

    @Override
    public String toString() {
        int prefixLength = getPrefixLength();
        if (prefixLength < 0) return toRangeString();

        StringBuilder sb = new StringBuilder();
        numericToTextFormat(sb, beginMsb, beginLsb, prefixLength);
        sb.append('/').append(prefixLength);
        return sb.toString();
    }

    @Override
    public String toRangeString() {
        StringBuilder sb = new StringBuilder();
        numericToTextFormat(sb, beginMsb, beginLsb, 128);
        sb.append(" - ");
        numericToTextFormat(sb, endMsb, endLsb, 128);
        return sb.toString();
    }

    public String toReverseDomain() {
        int prefixLength = getPrefixLength();
        if (prefixLength < 0) throw new IllegalArgumentException("Ipv6Interval " + toRangeString() + " is not a prefix");

        StringBuilder sb = new StringBuilder();
        // if prefixlength == 0, -1 >> 3 = -1, so this will not run
        for (int digit = 31 - ((prefixLength - 1) >> 2); digit < 32; digit++) {
            int b = reverseDomainDigit(beginMsb, beginLsb, digit);
            int e = reverseDomainDigit(endMsb, endLsb, digit);
            if (b == e) sb.append(HEX_DIGITS[b]);
            else sb.append(HEX_DIGITS[b]).append('-').append(HEX_DIGITS[e]);

            if (digit < 31) sb.append('.');
        }

        sb.append(IPV6_REVERSE_DOMAIN);

        return sb.toString();
    }

    // ipv6 address in reverse domain format consists of 32 hexadecimal digits
    int reverseDomainDigit(long msb, long lsb, int digit) {
        if (digit < 16) return (int) ((lsb >> (digit * 4))) & 0xf;
        else return (int) ((msb >> (digit - 16) * 4)) & 0xf;
    }


    @Override
    public String beginAddressAsString() {
        StringBuilder sb = new StringBuilder();
        numericToTextFormat(sb, beginMsb, beginLsb, 128);
        return sb.toString();
    }

    @Override
    public String endAddressAsString() {
        StringBuilder sb = new StringBuilder();
        numericToTextFormat(sb, endMsb, endLsb, 128);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (beginLsb ^ (beginLsb >>> 32));
        result = prime * result + (int) (beginMsb ^ (beginMsb >>> 32));
        result = prime * result + (int) (endLsb ^ (endLsb >>> 32));
        result = prime * result + (int) (endMsb ^ (endMsb >>> 32));
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
        Ipv6Interval other = (Ipv6Interval) obj;
        return compareTo(other) == 0;
    }

    public static long lsb(BigInteger begin) {
        return begin.and(MASK).longValue();
    }

    public static long msb(BigInteger begin) {
        return begin.shiftRight(LONG_BITCOUNT).longValue();
    }
}
