package com.bol.ipresource.ip;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Ipv4IntervalTest {

    private Ipv4Interval subject;

    // Some sugar
    private Matcher<Long> eq(long value) {
        return is(value);
    }

    private Matcher<Integer> eq(int value) {
        return is(value);
    }

    @Test
    public void parseValidIPv4Range() {
        subject = Ipv4Interval.parse("212.219.1.0 - 212.219.1.255");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("212.219.1.0"));
        assertThat(subject.endAddressAsString(), is("212.219.1.255"));
        assertThat(subject.toRangeString(), is("212.219.1.0 - 212.219.1.255"));
        assertThat(subject.getPrefixLength(), is(24));
    }

    @Test
    public void parseValidIPv4Address() {
        subject = Ipv4Interval.parse("212.219.1.0");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122432L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,0}));
        assertThat(subject.beginAddressAsString(), is("212.219.1.0"));
        assertThat(subject.endAddressAsString(), is("212.219.1.0"));
        assertThat(subject.toRangeString(), is("212.219.1.0 - 212.219.1.0"));
        assertThat(subject.getPrefixLength(), is(32));
    }

    @Test
    public void parseValidIPv4ARangeWithSlash() {
        subject = Ipv4Interval.parse("212.219.1.0/24");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("212.219.1.0"));
        assertThat(subject.endAddressAsString(), is("212.219.1.255"));
        assertThat(subject.toRangeString(), is("212.219.1.0 - 212.219.1.255"));
        assertThat(subject.getPrefixLength(), is(24));
    }

    @Test
    public void parseValidIPv4ARangeWithSlashAndNewLine() {
        subject = Ipv4Interval.parse("212.219.1.0/24\r\n");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)212,(byte)219,1,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("212.219.1.0"));
        assertThat(subject.endAddressAsString(), is("212.219.1.255"));
        assertThat(subject.toRangeString(), is("212.219.1.0 - 212.219.1.255"));
        assertThat(subject.getPrefixLength(), is(24));
    }

    @Test
    public void ipv4_with_prefix_21() {
        subject = Ipv4Interval.parse("151.64.0.1/21\r\n");
        assertThat(subject.begin(), eq(2537553920L));
        assertThat(subject.end(), eq(2537555967L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)151,64,0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)151,64,7,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("151.64.0.0"));
        assertThat(subject.endAddressAsString(), is("151.64.7.255"));
        assertThat(subject.toRangeString(), is("151.64.0.0 - 151.64.7.255"));
        assertThat(subject.getPrefixLength(), is(21));
    }

    @Test
    public void ipv4_with_prefix_23() {
        subject = Ipv4Interval.parse("109.73.65.0/23\r\n");
        assertThat(subject.begin(), eq(1833517056L));
        assertThat(subject.end(), eq(1833517567L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {109,73,64,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {109,73,65,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("109.73.64.0"));
        assertThat(subject.endAddressAsString(), is("109.73.65.255"));
        assertThat(subject.toRangeString(), is("109.73.64.0 - 109.73.65.255"));
        assertThat(subject.getPrefixLength(), is(23));
    }

    @Test
    public void ipv4_with_prefix_28() {
        subject = Ipv4Interval.parse("62.219.43.72/28\r\n");
        assertThat(subject.begin(), eq(1054550848L));
        assertThat(subject.end(), eq(1054550863L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {62,(byte)219,43,64}));
        assertThat(subject.endAsByteArray(), is(new byte[] {62,(byte)219,43,79}));
        assertThat(subject.beginAddressAsString(), is("62.219.43.64"));
        assertThat(subject.endAddressAsString(), is("62.219.43.79"));
        assertThat(subject.toRangeString(), is("62.219.43.64 - 62.219.43.79"));
        assertThat(subject.getPrefixLength(), is(28));
    }

    @Test
    public void ipv4_with_prefix_31() {
        subject = Ipv4Interval.parse("162.219.43.72/31\r\n");
        assertThat(subject.begin(), eq(2732272456L));
        assertThat(subject.end(), eq(2732272457L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)162,(byte)219,43,72}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)162,(byte)219,43,73}));
        assertThat(subject.beginAddressAsString(), is("162.219.43.72"));
        assertThat(subject.endAddressAsString(), is("162.219.43.73"));
        assertThat(subject.toRangeString(), is("162.219.43.72 - 162.219.43.73"));
        assertThat(subject.getPrefixLength(), is(31));
    }

    @Test
    public void ipv4_with_prefix_32() {
        subject = Ipv4Interval.parse("162.219.43.72/32\r\n");
        assertThat(subject.begin(), eq(2732272456L));
        assertThat(subject.end(), eq(2732272456L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {(byte)162,(byte)219,43,72}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)162,(byte)219,43,72}));
        assertThat(subject.beginAddressAsString(), is("162.219.43.72"));
        assertThat(subject.endAddressAsString(), is("162.219.43.72"));
        assertThat(subject.toRangeString(), is("162.219.43.72 - 162.219.43.72"));
        assertThat(subject.getPrefixLength(), is(32));
    }

    @Test
    public void zero_slash_zero_with_prefix_32() {
        subject = Ipv4Interval.parse("0/32\r\n");
        assertThat(subject.begin(), eq(0L));
        assertThat(subject.end(), eq(0L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0,0,0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {0,0,0,0}));
        assertThat(subject.beginAddressAsString(), is("0.0.0.0"));
        assertThat(subject.endAddressAsString(), is("0.0.0.0"));
        assertThat(subject.toRangeString(), is("0.0.0.0 - 0.0.0.0"));
        assertThat(subject.getPrefixLength(), is(32));
    }

    @Test
    public void zero_slash_zero_with_prefix_zero() {
        subject = Ipv4Interval.parse("0/0\r\n");
        assertThat(subject.begin(), eq(0L));
        assertThat(subject.end(), eq(4294967295L));
        assertThat(subject.beginAsByteArray(), is(new byte[] {0,0,0,0}));
        assertThat(subject.endAsByteArray(), is(new byte[] {(byte)255,(byte)255,(byte)255,(byte)255}));
        assertThat(subject.beginAddressAsString(), is("0.0.0.0"));
        assertThat(subject.endAddressAsString(), is("255.255.255.255"));
        assertThat(subject.toRangeString(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(subject.getPrefixLength(), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResource() {
        Ipv4Interval.parse("invalid resource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResourceType() {
        Ipv4Interval.parse("::0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginEndBeforeBeginFails() {
        subject = new Ipv4Interval(2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginOutOfBoundsFails() {
        subject = new Ipv4Interval(Long.MIN_VALUE, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEndOutOfBoundsFails() {
        subject = new Ipv4Interval(1, Long.MAX_VALUE);
    }

    @Test
    public void createReturnsCorrectBeginAndEnd() {
        subject = new Ipv4Interval(1, 2);

        assertThat(subject.begin(), eq(1L));
        assertThat(subject.end(), eq(2L));
    }

    @Test
    public void maxRangeContainsEverything() {
        assertTrue(Ipv4Interval.MAX_RANGE.contains(new Ipv4Interval(Ipv4Interval.MAX_RANGE.begin(), Ipv4Interval.MAX_RANGE.begin())));
        assertTrue(Ipv4Interval.MAX_RANGE.contains(new Ipv4Interval(Ipv4Interval.MAX_RANGE.end(), Ipv4Interval.MAX_RANGE.end())));
        assertTrue(Ipv4Interval.MAX_RANGE.contains(new Ipv4Interval(Ipv4Interval.MAX_RANGE.begin(), Ipv4Interval.MAX_RANGE.end())));
        assertTrue(Ipv4Interval.MAX_RANGE.contains(new Ipv4Interval(1231250, 123097120)));
    }

    @Test
    public void compareUpperBounds() {
        assertEquals(0, Ipv4Interval.MAX_RANGE.compareUpperBound(Ipv4Interval.MAX_RANGE));
        assertEquals(-1, Ipv4Interval.parse("127.0.0.0/8").compareUpperBound(Ipv4Interval.MAX_RANGE));
        assertEquals(1, Ipv4Interval.MAX_RANGE.compareUpperBound(Ipv4Interval.parse("127.0.0.0/8")));
    }

    @Test
    public void singletonIntervalAtLowerBound() {
        assertEquals(Ipv4Interval.parse("127.0.0.0/32"), Ipv4Interval.parse("127.0.0.0/8").singletonIntervalAtLowerBound());
    }

    @Test
    public void compareWorks() {
        subject = new Ipv4Interval(10, 20);

        assertThat(0, eq(subject.compareTo(subject)));
        assertThat(1, eq(subject.compareTo(new Ipv4Interval(0, 9))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Interval(21, 30))));

        assertThat(1, eq(subject.compareTo(new Ipv4Interval(0, 15))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Interval(15, 30))));

        assertThat(1, eq(subject.compareTo(new Ipv4Interval(0, 20))));
        assertThat(1, eq(subject.compareTo(new Ipv4Interval(10, 30))));

        assertThat(-1, eq(subject.compareTo(new Ipv4Interval(11, 30))));

        assertThat(-1, eq(subject.compareTo(new Ipv4Interval(10, 19))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Interval(11, 20))));
    }

    @Test
    public void verifyIntersects() {
        subject = new Ipv4Interval(10, 20);

        assertTrue(subject.intersects(subject));
        assertTrue(subject.intersects(Ipv4Interval.MAX_RANGE));

        assertFalse(subject.intersects(new Ipv4Interval(9, 9)));
        assertTrue(subject.intersects(new Ipv4Interval(9, 10)));
        assertTrue(subject.intersects(new Ipv4Interval(10, 11)));
        assertTrue(subject.intersects(new Ipv4Interval(5, 15)));

        assertFalse(subject.intersects(new Ipv4Interval(21, 21)));
        assertTrue(subject.intersects(new Ipv4Interval(19, 20)));
        assertTrue(subject.intersects(new Ipv4Interval(20, 21)));
        assertTrue(subject.intersects(new Ipv4Interval(15, 25)));
    }

    @Test
    public void verifyEquals() {
        subject = Ipv4Interval.parse("212.219.1.0/24");

        assertTrue(subject.equals(subject));
        assertFalse(subject.equals(Ipv4Interval.MAX_RANGE));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals("Random object"));
        assertThat(subject, not(Ipv4Interval.parse("212.218.1.0/24")));

        assertThat(subject, is(Ipv4Interval.parse("212.219.1.0 - 212.219.1.255")));
    }

    @Test
    public void verifyHashcode() {
        subject = Ipv4Interval.parse("212.219.1.0/24");
        Ipv4Interval test = Ipv4Interval.parse("212.219.1.0 - 212.219.1.255");

        assertThat(subject, is(test));
        assertThat(subject.hashCode(), is(test.hashCode()));
    }

    @Test
    public void toStringOfSlashNotation() {
        Ipv4Interval subject = Ipv4Interval.parse("212.219.1.0/24");

        assertThat(subject.toString(), is("212.219.1.0/24"));
    }

    @Test
    public void toStringOfDashNotation() {
        Ipv4Interval subject = Ipv4Interval.parse("212.219.1.0 - 212.219.1.255");

        assertThat(subject.toString(), is("212.219.1.0/24"));
    }

    @Test
    public void toStringOfSingleResource() {
        Ipv4Interval subject = Ipv4Interval.parse("212.219.1.0");

        assertThat(subject.toString(), is("212.219.1.0/32"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_empty() {
        Ipv4Interval.parseReverseDomain("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_null() {
        Ipv4Interval.parseReverseDomain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_inaddrarpa() {
        Ipv4Interval.parseReverseDomain("1.2.3.4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_octets() {
        Ipv4Interval.parseReverseDomain(".in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_more_than_four_octets() {
        Ipv4Interval.parseReverseDomain("8.7.6.5.4.3.2.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_dash_not_in_fourth_octet() {
        Ipv4Interval.parseReverseDomain("1-1.1.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_non_numeric_input() {
        Ipv4Interval.parseReverseDomain("1-1.b.a.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_multiple_dashes() {
        Ipv4Interval.parseReverseDomain("1-1.2-2.3-3.4-4.in-addr.arpa");
    }

    @Test
    public void reverse_simple() {
        assertThat(Ipv4Interval.parseReverseDomain("111.in-addr.arpa").toString(), is("111.0.0.0/8"));
        assertThat(Ipv4Interval.parseReverseDomain("22.111.in-addr.arpa").toString(), is("111.22.0.0/16"));
        assertThat(Ipv4Interval.parseReverseDomain("3.22.111.in-addr.arpa").toString(), is("111.22.3.0/24"));
        assertThat(Ipv4Interval.parseReverseDomain("4.3.22.111.in-addr.arpa").toString(), is("111.22.3.4/32"));
    }

    @Test
    public void reverse_simple_with_trailing_dot_and_mixed_caps() {
        assertThat(Ipv4Interval.parseReverseDomain("111.in-addr.arpa.").toString(), is("111.0.0.0/8"));
        assertThat(Ipv4Interval.parseReverseDomain("22.111.In-addr.arpa.").toString(), is("111.22.0.0/16"));
        assertThat(Ipv4Interval.parseReverseDomain("3.22.111.iN-aDdR.aRpA.").toString(), is("111.22.3.0/24"));
        assertThat(Ipv4Interval.parseReverseDomain("4.3.22.111.IN-ADDR.ARPA.").toString(), is("111.22.3.4/32"));
    }

    @Test
    public void reverse_with_range() {
        assertThat(Ipv4Interval.parseReverseDomain("44-55.33.22.11.in-addr.arpa.").toString(), is("11.22.33.44 - 11.22.33.55"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_inverse_range() {
        Ipv4Interval.parseReverseDomain("80-28.79.198.195.in-addr.arpa");
    }

    @Test
    public void parsePrefixWithLength() {
        assertThat(Ipv4Interval.parsePrefixWithLength(0, 0).toString(), is("0.0.0.0/0"));
        assertThat(Ipv4Interval.parsePrefixWithLength(0xffffffff, 0).toString(), is("0.0.0.0/0"));

        assertThat(Ipv4Interval.parsePrefixWithLength(0, 32).toString(), is("0.0.0.0/32"));
        assertThat(Ipv4Interval.parsePrefixWithLength(0xffffffff, 32).toString(), is("255.255.255.255/32"));

        assertThat(Ipv4Interval.parsePrefixWithLength(0xDEADBEEF, 13).toString(), is("222.168.0.0/13"));
        assertThat(Ipv4Interval.parsePrefixWithLength(0xCAFEBABE, 26).toString(), is("202.254.186.128/26"));
    }
}
