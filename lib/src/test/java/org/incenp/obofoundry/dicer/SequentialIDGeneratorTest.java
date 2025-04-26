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

public class SequentialIDGeneratorTest {

    @Test
    void testGenerateIDWithinRange() {
        IAutoIDGenerator gen = new SequentialIDGenerator("https://example.org/%07d", 1000, 2000, (id) -> false);

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertEquals(String.format("https://example.org/%07d", 1000 + i), id);
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testAvoidUsedLowerIDs() {
        HashSet<String> usedIDs = new HashSet<>();
        for ( int i = 1000; i < 1100; i++ ) {
            String id = String.format("https://example.org/%07d", i);
            usedIDs.add(id);
        }

        IAutoIDGenerator gen = new SequentialIDGenerator("https://example.org/%07d", 1000, 2000,
                (id) -> usedIDs.contains(id));

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertEquals(String.format("https://example.org/%07d", 1100 + i), id);
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testAvoidUsingMidRangeIDs() {
        HashSet<String> usedIDs = new HashSet<>();
        usedIDs.add("https://example.org/0001005");

        IAutoIDGenerator gen = new SequentialIDGenerator("https://example.org/%07d", 1000, 2000,
                (id) -> usedIDs.contains(id));

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                int expected = i < 5 ? i : i + 1;
                Assertions.assertEquals(String.format("https://example.org/%07d", 1000 + expected), id);
            }
        } catch ( IDNotFoundException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testFailUponOutOfIDSpace() {
        IAutoIDGenerator gen = new SequentialIDGenerator("https://example.org/%07d", 1000, 1005, (id) -> false);

        for ( int i = 0; i < 10; i++ ) {
            try {
                String id = gen.nextID();
                if ( i < 5 ) {
                    Assertions.assertEquals(String.format("https://example.org/%07d", 1000 + i), id);
                } else {
                    Assertions.fail("Expected OutOfIDSpaceException not thrown");
                }
            } catch ( IDNotFoundException e ) {
                if ( i < 5 ) {
                    Assertions.fail(e);
                } else {
                    Assertions.assertEquals("No available ID in range", e.getMessage());
                }
            }
        }
    }
}
