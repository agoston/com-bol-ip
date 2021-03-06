package com.bol.ipresource.etree;

import com.bol.ipresource.ip.Ipv4Interval;
import com.bol.ipresource.util.CollectionHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link NestedIntervalMap} using random data so we can flush out bugs
 * that we didn't expect.
 */
public class NestedIntervalMapRandomTest {

    private final long seed = System.currentTimeMillis();
    private final Random random = new Random(seed);

    private List<Ipv4Interval> everything;
    private Map<Ipv4Interval, List<Ipv4Interval>> childrenByParent;
    private NestedIntervalMap<Ipv4Interval, Ipv4Interval> subject;

    private List<Ipv4Interval> generateRandomSiblings(Ipv4Interval parent, int count) {
        List<Ipv4Interval> result = new ArrayList<>();
        if (count == 0)
            return result;

        long size = parent.end() - parent.begin() + 1;
        long sizePerChild = size / count;

        long start = parent.begin();
        for (int i = 0; i < count; ++i) {
            long gapBefore = sizePerChild * random.nextInt(4) / 10L;
            long gapAfter = sizePerChild * random.nextInt(1) / 10L;
            Ipv4Interval child = new Ipv4Interval(start + gapBefore, start + sizePerChild - gapAfter - 1);
            start += sizePerChild;
            assertTrue("generated child not inside parent (seed = " + seed + ")", parent.contains(child));
            if (!parent.equals(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private void generateRandomTree(Map<Ipv4Interval, List<Ipv4Interval>> result, Ipv4Interval parent, int depth, int siblingCount) {
        List<Ipv4Interval> children = generateRandomSiblings(parent, siblingCount * (7 + random.nextInt(7)) / 10);
        result.put(parent, children);

        if (depth > 0) {
            for (Ipv4Interval child : children) {
                generateRandomTree(result, child, depth - 1, siblingCount);
            }
        }
    }

    @Before
    public void setup() {
        everything = new ArrayList<>();
        childrenByParent = new HashMap<>();
        subject = new NestedIntervalMap<>();

        List<Ipv4Interval> roots = generateRandomSiblings(Ipv4Interval.MAX_RANGE, random.nextInt(3) + 5);
        for (Ipv4Interval root : roots) {
            generateRandomTree(childrenByParent, root, random.nextInt(4), random.nextInt(4) + 3);
        }
        everything.addAll(roots);
        for (List<Ipv4Interval> children : childrenByParent.values()) {
            everything.addAll(children);
        }
        for (Ipv4Interval interval : everything) {
            subject.put(interval, interval);
        }

        Collections.sort(everything);
//        System.err.println("RANDOM NESTED INTERVAL MAP TEST: Generated " + everything.size() + " intervals with seed " + seed);
    }

    @Test
    public void should_find_everything() {
        assertEquals("failed with seed: " + seed, everything, subject.findExactAndAllMoreSpecific(Ipv4Interval.MAX_RANGE));
    }

    @Test
    public void should_find_every_interval_individually() {
        for (Ipv4Interval interval : everything) {
            assertThat("failed with seed: " + seed, subject.findExact(interval), contains(interval));
        }
    }

    @Test
    public void should_find_all_more_specific() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Interval range = randomIpv4Interval();
            List<Ipv4Interval> actual = subject.findExactAndAllMoreSpecific(range);
            List<Ipv4Interval> expected = new ArrayList<>();
            for (Ipv4Interval interval : everything) {
                if (range.contains(interval)) {
                    expected.add(interval);
                }
            }
            assertEquals("failed with seed: " + seed, expected, actual);
        }
    }

    @Test
    public void should_find_all_less_specific() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Interval range = randomIpv4Interval();
            List<Ipv4Interval> actual = subject.findExactAndAllLessSpecific(range);
            List<Ipv4Interval> expected = new ArrayList<>();
            for (Ipv4Interval interval : everything) {
                if (interval.contains(range)) {
                    expected.add(interval);
                }
            }

            assertEquals("failed with seed: " + seed, expected, actual);
        }
    }

    @Test
    public void should_find_first_more_specific_for_every_contained_interval() {
        for (Ipv4Interval interval : childrenByParent.keySet()) {
            assertEquals("interval: " + interval + ", seed = " + seed, childrenByParent.get(interval), subject.findFirstMoreSpecific(interval));
        }
    }

    @Test
    public void should_promote_children_of_delete_node_to_parent() {
        for (int i = 0; i < 10; ) {
            NestedIntervalMap<Ipv4Interval, Ipv4Interval> copy = new NestedIntervalMap<>(subject);
            Ipv4Interval interval = everything.get(random.nextInt(everything.size()));
            if (childrenByParent.containsKey(interval)) {
                Ipv4Interval parent = CollectionHelper.uniqueResult(copy.findFirstLessSpecific(interval));
                if (parent != null) {
                    copy.remove(interval);
                    List<Ipv4Interval> actual = copy.findFirstMoreSpecific(parent);
                    assertTrue("interval " + interval + " did not move all children to parent " + parent + " on deletion (seed = " + seed + "): "
                            + actual, actual.containsAll(childrenByParent.get(interval)));
                    ++i;
                }
            }

        }
    }

    @Test
    public void should_contain_first_more_specific_for_random_intervals() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Interval range = randomIpv4Interval();
            List<Ipv4Interval> actual = subject.findFirstMoreSpecific(range);
            List<Ipv4Interval> allMoreSpecific = subject.findAllMoreSpecific(range);
            assertTrue("first more specific is subset of all more specific", allMoreSpecific.containsAll(actual));
            for (Ipv4Interval moreSpecific : allMoreSpecific) {
                boolean covered = false;
                for (Ipv4Interval firstMoreSpecific : actual) {
                    if (firstMoreSpecific.contains(moreSpecific)) {
                        covered = true;
                        break;
                    }
                }
                assertTrue("All more specific " + moreSpecific + " must be contained by first more specific", covered);
            }
        }
    }

    @Test
    public void should_remove_all_intervals_starting_with_child_nodes() {
        Collections.reverse(everything);
        for (Ipv4Interval interval : everything) {
            subject.remove(interval);
        }
        assertEquals(Collections.emptyList(), subject.findAllMoreSpecific(Ipv4Interval.MAX_RANGE));
    }

    private Ipv4Interval randomIpv4Interval() {
        return new Ipv4Interval(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE) + (Ipv4Interval.MAX_RANGE.end() / 2));
    }

}
