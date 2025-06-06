/*
 * Dicer - OBO ID range library
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDPolicyTest {

    @Test
    void testSimpleConstructor() {
        IDPolicy policy = new IDPolicy("myont");
        Assertions.assertEquals("http://purl.obolibrary.org/obo/myont", policy.getName());
        Assertions.assertEquals("http://purl.obolibrary.org/obo/MYONT_", policy.getPrefix());
        Assertions.assertEquals("MYONT", policy.getPrefixName());
        Assertions.assertEquals(7, policy.getWidth());
        Assertions.assertEquals(10000000, policy.getMaxUpperBound());
    }

    @Test
    void testFullConstructor() {
        IDPolicy policy = new IDPolicy("https://example.org/myont", "https://example.org/MYONT_", "MYONT", 8);
        Assertions.assertEquals("https://example.org/myont", policy.getName());
        Assertions.assertEquals("https://example.org/MYONT_", policy.getPrefix());
        Assertions.assertEquals("MYONT", policy.getPrefixName());
        Assertions.assertEquals(8, policy.getWidth());
        Assertions.assertEquals(100000000, policy.getMaxUpperBound());
    }

    @Test
    void testPolicyFormat() {
        IDPolicy policy = new IDPolicy("myont");
        Assertions.assertEquals("http://purl.obolibrary.org/obo/MYONT_%07d", policy.getFormat());
    }

    @Test
    void testPolicyRange() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            IDRange rng = policy.addRange("user1", null, 10000);
            Assertions.assertEquals("http://purl.obolibrary.org/obo/MYONT_%07d", rng.getFormat());
            Assertions.assertEquals(10000, rng.getSize());
            Assertions.assertEquals("id=1, name=user1, bounds=[0..10000)", rng.toString());
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testCreatingFirstRange() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            IDRange rng = policy.addRange("user1", null, 10000);

            Assertions.assertNotNull(rng);
            Assertions.assertEquals(0, rng.getLowerBound());
            Assertions.assertEquals(10000, rng.getUpperBound());
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testNotEnoughSpace() {
        IDPolicy policy = new IDPolicy("myont");
        // Create a range that takes up most of the available space
        try {
            policy.addRange("user1", null, 9000000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals(9000000, policy.findOpenRange(10000));
        Assertions.assertEquals(9000000, policy.findOpenRange(1000000));
        Assertions.assertEquals(-1, policy.findOpenRange(1000001));
    }

    @Test
    void testNotEnoughSpaceException() {
        IDPolicy policy = new IDPolicy("myont");
        // Create a range that takes up most of the available space
        try {
            policy.addRange("user1", null, 9000000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        try {
            policy.addRange("user2", null, 10000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        try {
            policy.addRange("user3", null, 1000000);
            Assertions.fail("Expected NoSuchIDRangeException not thrown");
        } catch ( IDRangeNotFoundException e ) {
            Assertions.assertEquals("Not enough space for a 1000000-wide range", e.getMessage());
        }
    }

    @Test
    void testFindingIntermediateRange() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 100000);
            policy.addRange(2, "user2", null, 150000, 200000);
            policy.addRange(3, "user3", null, 300000, 500000);
        } catch ( InvalidIDPolicyException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals(100000, policy.findOpenRange(50000));
        Assertions.assertEquals(200000, policy.findOpenRange(100000));
        Assertions.assertEquals(500000, policy.findOpenRange(100001));
    }

    @Test
    void testLookupByName() {
        IDPolicy policy = getTestPolicy();

        Assertions.assertEquals(0, policy.getRangeFor("user1").getLowerBound());
        Assertions.assertEquals(10000, policy.getRangeFor("user2").getLowerBound());
        Assertions.assertEquals(20000, policy.getRangeFor("user3").getLowerBound());
        Assertions.assertNull(policy.getRangeFor("user4"));
    }

    @Test
    void testLookupByNameWithOptional() {
        IDPolicy policy = getTestPolicy();

        Assertions.assertEquals(0, policy.findRange("user1").get().getLowerBound());
        Assertions.assertEquals(10000, policy.findRange("user2").get().getLowerBound());
        Assertions.assertEquals(20000, policy.findRange("user3").get().getLowerBound());
        Assertions.assertFalse(policy.findRange("user4").isPresent());
    }

    @Test
    void testLookupByNameWithException() {
        IDPolicy policy = getTestPolicy();

        try {
            Assertions.assertEquals(0, policy.getRange("user1").getLowerBound());
            Assertions.assertEquals(10000, policy.getRange("user2").getLowerBound());
            Assertions.assertEquals(20000, policy.getRange("user3").getLowerBound());
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        try {
            policy.getRange("user4");
            Assertions.fail("Expected NoSuchIDRangeException not thrown");
        } catch ( IDRangeNotFoundException e ) {
            Assertions.assertEquals("No range 'user4' found in ID policy", e.getMessage());
        }
    }

    @Test
    void testLookupAnyName() {
        IDPolicy policy = getTestPolicy();

        Assertions.assertEquals(10000,
                policy.findAnyRange(Arrays.asList("user4", "user2", "user3")).get().getLowerBound());
        Assertions.assertFalse(policy.findAnyRange(Arrays.asList("user4", "user5")).isPresent());
    }

    @Test
    void testLookupAnyNameWithException() {
        IDPolicy policy = getTestPolicy();

        try {
            Assertions.assertEquals(10000,
                    policy.getAnyRange(Arrays.asList("user4", "user2", "user3")).getLowerBound());
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        try {
            policy.getAnyRange(Arrays.asList("user4", "user5"));
            Assertions.fail("Expected NoSuchIDRangeException not thrown");
        } catch ( IDRangeNotFoundException e ) {
            Assertions.assertEquals("No suitable range found in ID policy", e.getMessage());
        }
    }

    @Test
    void testRangesByID() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(3, "user2", null, 10000, 20000);
            policy.addRange(2, "user3", null, 20000, 30000);
        } catch ( InvalidIDPolicyException e ) {
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
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 20000, 30000);
            policy.addRange(3, "user3", null, 10000, 20000);
        } catch ( InvalidIDPolicyException e ) {
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
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 5000, 15000);
            Assertions.fail("Overlapping range not detected");
        } catch ( InvalidIDPolicyException e ) {
            Assertions.assertEquals("Range [5000..15000) for \"user2\" overlaps with range [0..10000) for \"user1\"",
                    e.getMessage());
        }
    }

    @Test
    void testUnallocatedRanges() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 10000, 20000);
            policy.addRange(2, "user2", null, 50000, 100000);
        } catch ( InvalidIDPolicyException e ) {
            Assertions.fail(e);
        }

        List<IDRange> unallocated = policy.getUnallocatedRanges();
        Assertions.assertEquals(3, unallocated.size());
        Assertions.assertEquals(0, unallocated.get(0).getLowerBound());
        Assertions.assertEquals(10000, unallocated.get(0).getUpperBound());
        Assertions.assertEquals(20000, unallocated.get(1).getLowerBound());
        Assertions.assertEquals(50000, unallocated.get(1).getUpperBound());
        Assertions.assertEquals(100000, unallocated.get(2).getLowerBound());
        Assertions.assertEquals(policy.getMaxUpperBound(), unallocated.get(2).getUpperBound());
    }

    private IDPolicy getTestPolicy() {
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange(1, "user1", null, 0, 10000);
            policy.addRange(2, "user2", null, 10000, 20000);
            policy.addRange(3, "user3", null, 20000, 30000);
        } catch (InvalidIDPolicyException e) {
            Assertions.fail(e);
        }
        return policy;
    }
}
