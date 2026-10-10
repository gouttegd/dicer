/*
 * Dicer - OBO ID range library
 * Copyright © 2025 Damien Goutte-Gattat
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   (1) Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   (2) Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 *   (3) Neither the name of the copyright holder nor the names its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
    void testInitWithIDRange() {
        IDPolicy policy = new IDPolicy("myont");
        IDRange rng = null;
        try {
            rng = policy.addRange("user1", null, 1000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        IAutoIDGenerator gen = new SequentialIDGenerator(rng, (id) -> false);

        try {
            for ( int i = 0; i < 10; i++ ) {
                String id = gen.nextID();
                Assertions.assertEquals(String.format("http://purl.obolibrary.org/obo/MYONT_%07d", i), id);
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
