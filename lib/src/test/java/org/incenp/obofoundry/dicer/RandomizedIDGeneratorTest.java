/*
 * Dicer - OBO ID range library
 * Copyright © 2025 Damien Goutte-Gattat
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

import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RandomizedIDGeneratorTest {

    @Test
    void testGenerateIDWithinRange() {
        IAutoIDGenerator gen = new RandomizedIDGenerator("https://example.org/%07d", 1000, 2000, (id) -> false);

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertTrue(id.startsWith("https://example.org/0001"));
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testInitWithIDRange() {
        IDPolicy policy = new IDPolicy("myont");
        IDRange rng = null;
        try {
            policy.addRange("user1", null, 1000);
            rng = policy.addRange("user2", null, 1000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        IAutoIDGenerator gen = new RandomizedIDGenerator(rng, (id) -> false);

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertTrue(id.startsWith("http://purl.obolibrary.org/obo/MYONT_0001"));
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testAvoidUsedLowerIDs() {
        HashSet<String> usedIDs = new HashSet<>();
        for ( int i = 1000; i < 1100; i++ ) {
            usedIDs.add(String.format("https://example.org/%07d", i));
        }

        IAutoIDGenerator gen = new RandomizedIDGenerator("https://example.org/%07d", 1000, 2000,
                (id) -> usedIDs.contains(id));

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertTrue(id.startsWith("https://example.org/0001"));
                Assertions.assertFalse(id.startsWith("https://example/org/00010"));
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testFailUponOutOfIDSpace() {
        HashSet<String> usedIDs = new HashSet<String>();
        for ( int i = 1000; i < 1100; i++ ) {
            usedIDs.add(String.format("https://example.org/%07d", i));
        }

        IAutoIDGenerator gen = new RandomizedIDGenerator("https://example.org/%07d", 1000, 1200,
                (id) -> usedIDs.contains(id));

        try {
            for ( int i = 0; i < 100; i++ ) {
                gen.nextID();
            }
            Assertions.fail("Expected IDNotFoundException not thrown", null);
        } catch ( IDNotFoundException e ) {
            Assertions.assertEquals("No available ID in range", e.getMessage());
        }
    }
}
