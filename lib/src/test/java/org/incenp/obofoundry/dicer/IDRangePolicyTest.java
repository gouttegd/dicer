/*
 * Dicer - OBO ID range library
 * Copyright © 2024 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.dicer;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDRangePolicyTest {

    @Test
    void testSimpleConstructor() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        Assertions.assertEquals("http://purl.obolibrary.org/obo/myont", policy.getName());
        Assertions.assertEquals("http://purl.obolibrary.org/obo/MYONT_", policy.getPrefix());
        Assertions.assertEquals("MYONT", policy.getPrefixName());
        Assertions.assertEquals(7, policy.getWidth());
        Assertions.assertEquals(10000000, policy.getMaxUpperBound());
    }

    @Test
    void testFullConstructor() {
        IDRangePolicy policy = new IDRangePolicy("https://example.org/myont", "https://example.org/MYONT_", "MYONT", 8);
        Assertions.assertEquals("https://example.org/myont", policy.getName());
        Assertions.assertEquals("https://example.org/MYONT_", policy.getPrefix());
        Assertions.assertEquals("MYONT", policy.getPrefixName());
        Assertions.assertEquals(8, policy.getWidth());
        Assertions.assertEquals(100000000, policy.getMaxUpperBound());
    }

    @Test
    void testCreatingFirstRange() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            IDRange rng = policy.addRange("user1", null, 10000);

            Assertions.assertNotNull(rng);
            Assertions.assertEquals(0, rng.getLowerBound());
            Assertions.assertEquals(10000, rng.getUpperBound());
        } catch ( OutOfIDSpaceException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testNotEnoughSpace() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        // Create a range that takes up most of the available space
        try {
            policy.addRange("user1", null, 9000000);
        } catch ( OutOfIDSpaceException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals(9000000, policy.findOpenRange(10000));
        Assertions.assertEquals(9000000, policy.findOpenRange(1000000));
        Assertions.assertEquals(-1, policy.findOpenRange(1000001));
    }

    @Test
    void testNotEnoughSpaceException() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        // Create a range that takes up most of the available space
        try {
            policy.addRange("user1", null, 9000000);
        } catch ( OutOfIDSpaceException e ) {
            Assertions.fail(e);
        }

        try {
            policy.addRange("user2", null, 10000);
        } catch ( OutOfIDSpaceException e ) {
            Assertions.fail(e);
        }

        try {
            policy.addRange("user3", null, 1000000);
            Assertions.fail("Expected OutOfIDSpaceException not thrown");
        } catch ( OutOfIDSpaceException e ) {
            Assertions.assertEquals("Not enough space for a 1000000-wide range", e.getMessage());
        }
    }

    @Test
    void testFindingIntermediateRange() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 100000);
            policy.addRange(2, "user2", null, 150000, 200000);
            policy.addRange(3, "user3", null, 300000, 500000);
        } catch ( InvalidIDRangePolicyException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals(100000, policy.findOpenRange(50000));
        Assertions.assertEquals(200000, policy.findOpenRange(100000));
        Assertions.assertEquals(500000, policy.findOpenRange(100001));
    }

    @Test
    void testLookupByName() {
        IDRangePolicy policy = getTestPolicy();

        Assertions.assertEquals(0, policy.getRangeFor("user1").getLowerBound());
        Assertions.assertEquals(10000, policy.getRangeFor("user2").getLowerBound());
        Assertions.assertEquals(20000, policy.getRangeFor("user3").getLowerBound());
        Assertions.assertNull(policy.getRangeFor("user4"));
    }

    @Test
    void testLookupByNameWithOptional() {
        IDRangePolicy policy = getTestPolicy();

        Assertions.assertEquals(0, policy.findRange("user1").get().getLowerBound());
        Assertions.assertEquals(10000, policy.findRange("user2").get().getLowerBound());
        Assertions.assertEquals(20000, policy.findRange("user3").get().getLowerBound());
        Assertions.assertFalse(policy.findRange("user4").isPresent());
    }

    @Test
    void testLookupByNameWithException() {
        IDRangePolicy policy = getTestPolicy();

        try {
            Assertions.assertEquals(0, policy.getRange("user1").getLowerBound());
            Assertions.assertEquals(10000, policy.getRange("user2").getLowerBound());
            Assertions.assertEquals(20000, policy.getRange("user3").getLowerBound());
        } catch ( NoSuchIDRangeException e ) {
            Assertions.fail(e);
        }

        try {
            policy.getRange("user4");
            Assertions.fail("Expected NoSuchIDRangeException not thrown");
        } catch ( NoSuchIDRangeException e ) {
            Assertions.assertEquals("No range 'user4' found in ID policy", e.getMessage());
        }
    }

    @Test
    void testRangesByID() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(3, "user2", null, 10000, 20000);
            policy.addRange(2, "user3", null, 20000, 30000);
        } catch ( InvalidIDRangePolicyException e ) {
            Assertions.fail(e);
        }

        List<IDRange> ranges = policy.getRangesByID();
        Assertions.assertEquals(3, ranges.size());
        Assertions.assertEquals(0, ranges.get(0).getLowerBound());
        Assertions.assertEquals(20000, ranges.get(1).getLowerBound());
        Assertions.assertEquals(10000, ranges.get(2).getLowerBound());
    }

    @Test
    void testRangesByLowerBound() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 20000, 30000);
            policy.addRange(3, "user3", null, 10000, 20000);
        } catch ( InvalidIDRangePolicyException e ) {
            Assertions.fail(e);
        }

        List<IDRange> ranges = policy.getRangesByLowerBound();
        Assertions.assertEquals(3, ranges.size());
        Assertions.assertEquals(0, ranges.get(0).getLowerBound());
        Assertions.assertEquals(10000, ranges.get(1).getLowerBound());
        Assertions.assertEquals(20000, ranges.get(2).getLowerBound());
    }

    @Test
    void testOverlappingRanges() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 5000, 15000);
            Assertions.fail("Overlapping range not detected");
        } catch ( InvalidIDRangePolicyException e ) {
            Assertions.assertEquals("Range [5000..15000) for \"user2\" overlaps with range [0..10000) for \"user1\"",
                    e.getMessage());
        }
    }

    private IDRangePolicy getTestPolicy() {
        IDRangePolicy policy = new IDRangePolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 10000, 20000);
            policy.addRange(3, "user3", null, 20000, 30000);
        } catch (InvalidIDRangePolicyException e) {
            Assertions.fail(e);
        }
        return policy;
    }
}
