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
