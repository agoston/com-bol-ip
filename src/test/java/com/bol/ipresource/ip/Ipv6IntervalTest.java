package com.bol.ipresource.ip;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Ipv6IntervalTest {
    private Ipv6Interval subject;

    private static Ipv6Interval resource(long begin, long end) {
        return new Ipv6Interval(BigInteger.valueOf(begin), BigInteger.valueOf(end));
    }

    @Test
    public void parseValidIPv6Prefix() {
        subject = Ipv6Interval.parse("::f000/116");
        assertThat(subject.beginAsBigInteger(), is(BigInteger.valueOf(61440)));
        assertThat(subject.endAsBigInteger(), is(BigInteger.valueOf(65535)));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,(byte)0xf0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,(byte)0xff,(byte)0xff}));
        assertThat(subject.beginAddressAsString(), is("::f000"));
        assertThat(subject.endAddressAsString(), is("::ffff"));
        assertThat(subject.toRangeString(), is("::f000 - ::ffff"));
        assertThat(subject.getPrefixLength(), is(116));
    }

    @Test
    public void parseValidIPv6Address() {
        subject = Ipv6Interval.parse("::2001");
        assertThat(subject.beginAsBigInteger(), is(BigInteger.valueOf(8193)));
        assertThat(subject.endAsBigInteger(), is(BigInteger.valueOf(8193)));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x20,0x01}));
        assertThat(subject.endAsByteArray(), is(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,(byte)0x20,(byte)0x01}));
        assertThat(subject.beginAddressAsString(), is("::2001"));
        assertThat(subject.endAddressAsString(), is("::2001"));
        assertThat(subject.toRangeString(), is("::2001 - ::2001"));
        assertThat(subject.getPrefixLength(), is(128));
    }

    @Test
    public void parseValidIPv6ARangeWithSlash() {
        subject = Ipv6Interval.parse("2001::/64");
        assertThat(subject.beginAsBigInteger(), is(new BigInteger("42540488161975842760550356425300246528")));
        assertThat(subject.endAsBigInteger(), is(new BigInteger("42540488161975842778997100499009798143")));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0x20,0x01,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {0x20,0x01,0,0,0,0,0,0,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff}));
        assertThat(subject.beginAddressAsString(), is("2001::"));
        assertThat(subject.endAddressAsString(), is("2001::ffff:ffff:ffff:ffff"));
        assertThat(subject.toRangeString(), is("2001:: - 2001::ffff:ffff:ffff:ffff"));
        assertThat(subject.getPrefixLength(), is(64));
    }

    @Test
    public void valid_ipv6_with_prefix_48() {
        subject = Ipv6Interval.parse("2a00:1f78::fffe/48");
        assertThat(subject.beginAsBigInteger(), is(new BigInteger("55828214085043681575463550121838379008")));
        assertThat(subject.endAsBigInteger(), is(new BigInteger("55828214085044890501283164751013085183")));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0x2a,0,0x1f,0x78,0,0,0,0,0,0,0,0,0,0,0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {0x2a,0,0x1f,0x78,0,0,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff}));
        assertThat(subject.beginAddressAsString(), is("2a00:1f78::"));
        assertThat(subject.endAddressAsString(), is("2a00:1f78::ffff:ffff:ffff:ffff:ffff"));
        assertThat(subject.toRangeString(), is("2a00:1f78:: - 2a00:1f78::ffff:ffff:ffff:ffff:ffff"));
        assertThat(subject.getPrefixLength(), is(48));
    }

    @Test
    public void parseValidIPv6ARangeWithSlashAndNewLine() {
        subject = Ipv6Interval.parse("2001::/64\r\n");
        assertThat(subject.beginAsBigInteger(), is(new BigInteger("42540488161975842760550356425300246528")));
        assertThat(subject.endAsBigInteger(), is(new BigInteger("42540488161975842778997100499009798143")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIpv6MappedIpv4Fails() {
        subject = Ipv6Interval.parse("::ffff:192.0.2.128");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResource() {
        Ipv6Interval.parse("invalid resource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResourceType() {
        Ipv6Interval.parse("12.0.0.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void Ipv6RangeThrowsIllegalArgumentException() {
        Ipv6Interval.parse("2001:: - 2020::");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginEndBeforeBeginFails() {
        subject = resource(2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginOutOfBoundsFails() {
        subject = resource(Long.MIN_VALUE, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEndOutOfBoundsFails() {
        subject = new Ipv6Interval(BigInteger.ONE, BigInteger.ONE.shiftLeft(128));
    }

    @Test
    public void createReturnsCorrectBeginAndEnd() {
        subject = resource(1, 2);

        assertThat(subject.beginAsBigInteger(), is(BigInteger.valueOf(1L)));
        assertThat(subject.endAsBigInteger(), is(BigInteger.valueOf(2L)));
    }

    @Test
    public void maxRangeContainsEverything() {
        assertTrue(Ipv6Interval.MAX_RANGE.contains(new Ipv6Interval(Ipv6Interval.MAX_RANGE.beginAsBigInteger(), Ipv6Interval.MAX_RANGE.beginAsBigInteger())));
        assertTrue(Ipv6Interval.MAX_RANGE.contains(new Ipv6Interval(Ipv6Interval.MAX_RANGE.endAsBigInteger(), Ipv6Interval.MAX_RANGE.endAsBigInteger())));
        assertTrue(Ipv6Interval.MAX_RANGE.contains(new Ipv6Interval(Ipv6Interval.MAX_RANGE.beginAsBigInteger(), Ipv6Interval.MAX_RANGE.endAsBigInteger())));
        assertTrue(Ipv6Interval.MAX_RANGE.contains(resource(1231250, 123097120)));
    }

    @Test
    public void compareUpperBounds() {
        assertEquals(0, Ipv6Interval.MAX_RANGE.compareUpperBound(Ipv6Interval.MAX_RANGE));
        assertEquals(-1, Ipv6Interval.parse("2001:ffce::/32").compareUpperBound(Ipv6Interval.MAX_RANGE));
        assertEquals(1, Ipv6Interval.MAX_RANGE.compareUpperBound(Ipv6Interval.parse("2001:ffce::/32")));
    }

    @Test
    public void singletonIntervalAtLowerBound() {
        assertEquals(Ipv6Interval.parse("2001::/128"), Ipv6Interval.parse("2001::/77").singletonIntervalAtLowerBound());
    }

    @Test
    public void compareWorks() {
        subject = resource(10, 20);

        assertThat(0, is(subject.compareTo(subject)));
        assertThat(1, is(subject.compareTo(resource(0, 9))));
        assertThat(-1, is(subject.compareTo(resource(21, 30))));

        assertThat(1, is(subject.compareTo(resource(0, 15))));
        assertThat(-1, is(subject.compareTo(resource(15, 30))));

        assertThat(1, is(subject.compareTo(resource(0, 20))));
        assertThat(1, is(subject.compareTo(resource(10, 30))));

        assertThat(-1, is(subject.compareTo(resource(11, 30))));

        assertThat(-1, is(subject.compareTo(resource(10, 19))));
        assertThat(-1, is(subject.compareTo(resource(11, 20))));
    }

    @Test
    public void truncateByPrefixLength() {
        assertThat(Ipv6Interval.parse("2001:2002:2003:2004:1::/65").toString(), is("2001:2002:2003:2004::/65"));
    }

    @Test
    public void verifyIntersects() {
        subject = resource(10, 20);

        assertTrue(subject.intersects(subject));
        assertTrue(subject.intersects(Ipv6Interval.MAX_RANGE));

        assertFalse(subject.intersects(resource(9, 9)));
        assertTrue(subject.intersects(resource(9, 10)));
        assertTrue(subject.intersects(resource(9, 15)));
        assertTrue(subject.intersects(resource(9, 20)));
        assertTrue(subject.intersects(resource(9, 21)));

        assertTrue(subject.intersects(resource(10, 10)));
        assertTrue(subject.intersects(resource(10, 15)));
        assertTrue(subject.intersects(resource(10, 20)));
        assertTrue(subject.intersects(resource(10, 21)));

        assertTrue(subject.intersects(resource(15, 15)));
        assertTrue(subject.intersects(resource(15, 20)));
        assertTrue(subject.intersects(resource(15, 21)));

        assertTrue(subject.intersects(resource(20, 20)));
        assertTrue(subject.intersects(resource(20, 21)));

        assertFalse(subject.intersects(resource(21, 21)));
    }

    @Test
    public void verifyEquals() {
        subject = Ipv6Interval.parse("2001::/64");

        assertTrue(subject.equals(subject));
        assertFalse(subject.equals(Ipv6Interval.MAX_RANGE));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals("Random object"));
        assertThat(subject, not(Ipv6Interval.parse("ffce::/64")));

        assertThat(subject, is(Ipv6Interval.parse("2001:0:0:0:0:1:2:3/64")));
    }

    @Test
    public void verifyHashcode() {
        subject = Ipv6Interval.parse("2001::/64");
        Ipv6Interval test = Ipv6Interval.parse("2001:0:0:0:0::2:3/64");

        assertThat(subject, is(test));
        assertThat(subject.hashCode(), is(test.hashCode()));
    }

    @Test
    public void toStringOfSlashNotation() {
        assertThat(Ipv6Interval.parse("2001::/64").toString(), is("2001::/64"));
        assertThat(Ipv6Interval.parse("2001:0:0:0:0::2:3/64").toString(), is("2001::/64"));
        assertThat(Ipv6Interval.parse("2001:00:0000::0/64").toString(), is("2001::/64"));
    }

    @Test
    public void toStringBitBoundaryTest() {
        assertThat(Ipv6Interval.parse("1234:4567:89ab::/20").toString(), is("1234:4000::/20"));
        assertThat(Ipv6Interval.parse("1234:4567:89ab::/15").toString(), is("1234::/15"));
        assertThat(Ipv6Interval.parse("1234:4567:89ab::/14").toString(), is("1234::/14"));
        assertThat(Ipv6Interval.parse("1234:4567:89ab::/13").toString(), is("1230::/13"));
        assertThat(Ipv6Interval.parse("1234:4567:89ab::/12").toString(), is("1230::/12"));
        assertThat(Ipv6Interval.parse("ffff:4567:89ab:cdef::/4").toString(), is("f000::/4"));
        assertThat(Ipv6Interval.parse("ffff:4567:89ab:cdef::/3").toString(), is("e000::/3"));
        assertThat(Ipv6Interval.parse("ffff:4567:89ab:cdef::/2").toString(), is("c000::/2"));
        assertThat(Ipv6Interval.parse("ffff:4567:89ab:cdef::/1").toString(), is("8000::/1"));
        assertThat(Ipv6Interval.parse("1234:4567:89ab:cdef::/0").toString(), is("::/0"));
    }

    @Test
    public void toStringOfSingleResource() {
        assertThat(Ipv6Interval.parse("2001::").toString(), is("2001::/128"));
    }

    @Test
    public void toStringOfEntireAddressSpace() {
        assertThat(Ipv6Interval.parse("::/0").toString(), is("::/0"));
    }

    @Test
    public void toStringOfLocalhost() {
        assertThat(Ipv6Interval.parse("::1").toString(), is("::1/128"));
    }

    @Test
    public void toStringOfZero() {
        assertThat(Ipv6Interval.parse("::").toString(), is("::/128"));
    }

    private int compare(long aMsb, long aLsb, long bMsb, long bLsb) {
        return Ipv6Interval.compare(aMsb, aLsb, bMsb, bLsb);
    }

    @Test
    public void doubleLongUnsignedComparison() {
        assertThat(compare( 0,  0,  0,  0), is(0));
        assertThat(compare( 0,  0,  0, -1), is(-1));
        assertThat(compare( 0,  0,  0,  1), is(-1));

        assertThat(compare( 0,  0, -1,  0), is(-1));
        assertThat(compare( 0,  0, -1, -1), is(-1));
        assertThat(compare( 0,  0, -1,  1), is(-1));

        assertThat(compare( 0,  0,  1,  0), is(-1));
        assertThat(compare( 0,  0,  1, -1), is(-1));
        assertThat(compare( 0,  0,  1,  1), is(-1));

        assertThat(compare( 0, -1,  0,  0), is(1));
        assertThat(compare( 0, -1,  0, -1), is(0));
        assertThat(compare( 0, -1,  0,  1), is(1));

        assertThat(compare( 0, -1, -1,  0), is(-1));
        assertThat(compare( 0, -1, -1, -1), is(-1));
        assertThat(compare( 0, -1, -1,  1), is(-1));

        assertThat(compare( 0, -1,  1,  0), is(-1));
        assertThat(compare( 0, -1,  1, -1), is(-1));
        assertThat(compare( 0, -1,  1,  1), is(-1));

        assertThat(compare( 0,  1,  0,  0), is(1));
        assertThat(compare( 0,  1,  0, -1), is(-1));
        assertThat(compare( 0,  1,  0,  1), is(0));

        assertThat(compare( 0,  1, -1,  0), is(-1));
        assertThat(compare( 0,  1, -1, -1), is(-1));
        assertThat(compare( 0,  1, -1,  1), is(-1));

        assertThat(compare( 0,  1,  1,  0), is(-1));
        assertThat(compare( 0,  1,  1, -1), is(-1));
        assertThat(compare( 0,  1,  1,  1), is(-1));

        assertThat(compare(-1,  0,  0,  0), is(1));
        assertThat(compare(-1,  0,  0, -1), is(1));
        assertThat(compare(-1,  0,  0,  1), is(1));

        assertThat(compare(-1,  0, -1,  0), is(0));
        assertThat(compare(-1,  0, -1, -1), is(-1));
        assertThat(compare(-1,  0, -1,  1), is(-1));

        assertThat(compare(-1,  0,  1,  0), is(1));
        assertThat(compare(-1,  0,  1, -1), is(1));
        assertThat(compare(-1,  0,  1,  1), is(1));

        assertThat(compare(-1, -1,  0,  0), is(1));
        assertThat(compare(-1, -1,  0, -1), is(1));
        assertThat(compare(-1, -1,  0,  1), is(1));

        assertThat(compare(-1, -1, -1,  0), is(1));
        assertThat(compare(-1, -1, -1, -1), is(0));
        assertThat(compare(-1, -1, -1,  1), is(1));

        assertThat(compare(-1, -1,  1,  0), is(1));
        assertThat(compare(-1, -1,  1, -1), is(1));
        assertThat(compare(-1, -1,  1,  1), is(1));

        assertThat(compare(-1,  1,  0,  0), is(1));
        assertThat(compare(-1,  1,  0, -1), is(1));
        assertThat(compare(-1,  1,  0,  1), is(1));

        assertThat(compare(-1,  1, -1,  0), is(1));
        assertThat(compare(-1,  1, -1, -1), is(-1));
        assertThat(compare(-1,  1, -1,  1), is(0));

        assertThat(compare(-1,  1,  1,  0), is(1));
        assertThat(compare(-1,  1,  1, -1), is(1));
        assertThat(compare(-1,  1,  1,  1), is(1));

        assertThat(compare( 1,  0,  0,  0), is(1));
        assertThat(compare( 1,  0,  0, -1), is(1));
        assertThat(compare( 1,  0,  0,  1), is(1));

        assertThat(compare( 1,  0, -1,  0), is(-1));
        assertThat(compare( 1,  0, -1, -1), is(-1));
        assertThat(compare( 1,  0, -1,  1), is(-1));

        assertThat(compare( 1,  0,  1,  0), is(0));
        assertThat(compare( 1,  0,  1, -1), is(-1));
        assertThat(compare( 1,  0,  1,  1), is(-1));

        assertThat(compare( 1, -1,  0,  0), is(1));
        assertThat(compare( 1, -1,  0, -1), is(1));
        assertThat(compare( 1, -1,  0,  1), is(1));

        assertThat(compare( 1, -1, -1,  0), is(-1));
        assertThat(compare( 1, -1, -1, -1), is(-1));
        assertThat(compare( 1, -1, -1,  1), is(-1));

        assertThat(compare( 1, -1,  1,  0), is(1));
        assertThat(compare( 1, -1,  1, -1), is(0));
        assertThat(compare( 1, -1,  1,  1), is(1));

        assertThat(compare( 1,  1,  0,  0), is(1));
        assertThat(compare( 1,  1,  0, -1), is(1));
        assertThat(compare( 1,  1,  0,  1), is(1));

        assertThat(compare( 1,  1, -1,  0), is(-1));
        assertThat(compare( 1,  1, -1, -1), is(-1));
        assertThat(compare( 1,  1, -1,  1), is(-1));

        assertThat(compare( 1,  1,  1,  0), is(1));
        assertThat(compare( 1,  1,  1, -1), is(-1));
        assertThat(compare( 1,  1,  1,  1), is(0));

    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_empty() {
        Ipv6Interval.parseReverseDomain("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_null() {
        Ipv6Interval.parseReverseDomain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_ip6arpa() {
        Ipv6Interval.parseReverseDomain("1.2.3.4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_octets() {
        Ipv6Interval.parseReverseDomain(".ip6.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_more_than_four_octets() {
        Ipv6Interval.parseReverseDomain("8.7.6.5.4.3.2.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_invalid_nibbles_dash() {
        Ipv6Interval.parseReverseDomain("1-1.1.a.ip6.arpa");
    }
    @Test(expected = IllegalArgumentException.class)
    public void reverse_invalid_nibbles_non_hex() {
        Ipv6Interval.parseReverseDomain("g.ip6.arpa");
    }

    @Test
    public void reverse_simple() {
        assertThat(Ipv6Interval.parseReverseDomain("2.ip6.arpa").toString(), is("2000::/4"));
        assertThat(Ipv6Interval.parseReverseDomain("8.3.7.0.1.0.0.2.ip6.arpa").toString(), is("2001:738::/32"));
        assertThat(Ipv6Interval.parseReverseDomain("a.7.9.b.1.1.0.2.8.b.7.0.1.0.a.2.ip6.arpa").toString(), is("2a01:7b8:2011:b97a::/64"));
        assertThat(Ipv6Interval.parseReverseDomain("b.a.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89ab/128"));
        assertThat(Ipv6Interval.parseReverseDomain("B.A.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.B.D.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89ab/128"));
        assertThat(Ipv6Interval.parseReverseDomain("a.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89a0/124"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_prefix_length() {
        Ipv6Interval.parse("2001::/129");
    }

    @Test
    public void toByteArray() {
        assertThat(Ipv6Interval.parse(Ipv6Interval.parse("2001::/112").beginAsInetAddress()), is(Ipv6Interval.parse("2001::/128")));
        assertThat(Ipv6Interval.parse(Ipv6Interval.parse("2001::/112").endAsInetAddress()), is(Ipv6Interval.parse("2001::ffff/128")));

        assertThat(Ipv6Interval.parse(Ipv6Interval.parse("2001::/16").beginAsInetAddress()), is(Ipv6Interval.parse("2001::/128")));
        assertThat(Ipv6Interval.parse(Ipv6Interval.parse("2001::/16").endAsInetAddress()), is(Ipv6Interval.parse("2001:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128")));
    }
}
