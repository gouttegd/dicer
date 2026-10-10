/*
 * Dicer - OBO ID range library
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDPolicyReaderTest {

    @Test
    void testReadUberonPolicy() {
        IDPolicyReader p = new IDPolicyReader();
        IDPolicy policy = null;
        try {
            policy = p.read("src/test/resources/input/uberon-idranges.owl");
        } catch ( IOException e ) {
            Assertions.fail(e);
        } catch ( InvalidIDPolicyException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals("http://purl.obolibrary.org/obo/uberon", policy.getName());
        Assertions.assertEquals("http://purl.obolibrary.org/obo/UBERON_", policy.getPrefix());
        Assertions.assertEquals("UBERON", policy.getPrefixName());
        Assertions.assertEquals(7, policy.getWidth());

        IDRange own = policy.getRangeFor("Damien Goutte-Gattat");
        Assertions.assertNotNull(own);
        Assertions.assertEquals(28, own.getID());
        Assertions.assertEquals(8450001, own.getLowerBound());
        Assertions.assertEquals(8460000, own.getUpperBound());
    }

    @Test
    void testReadSimplePolicy() {
        IDPolicyReader p = new IDPolicyReader();
        IDPolicy policy = null;
        try {
            policy = p.read("src/test/resources/input/myont-idranges.owl");
        } catch ( IOException e ) {
            Assertions.fail(e);
        } catch ( InvalidIDPolicyException e ) {
            Assertions.fail(e);
        }

        Assertions.assertEquals("http://purl.obolibrary.org/obo/myont", policy.getName());
        Assertions.assertEquals("http://purl.obolibrary.org/obo/MYONT_", policy.getPrefix());
        Assertions.assertEquals("MYONT", policy.getPrefixName());
        Assertions.assertEquals(7, policy.getWidth());

        IDRange rng = policy.getRangeFor("user1");
        Assertions.assertNotNull(rng);
        Assertions.assertEquals(1, rng.getID());
        Assertions.assertEquals(0, rng.getLowerBound());
        Assertions.assertEquals(10000, rng.getUpperBound());

        rng = policy.getRangeFor("user2");
        Assertions.assertNotNull(rng);
        Assertions.assertEquals(2, rng.getID());
        Assertions.assertEquals(10000, rng.getLowerBound());
        Assertions.assertEquals(30000, rng.getUpperBound());
    }

    @Test
    void testReadInvalidPolicy() {
        IDPolicyReader p = new IDPolicyReader();

        String[] invalidFiles = {
                "invalid-ontology-idranges.owl",
                "invalid-non-literal-annotation-idranges.owl",
                "invalid-non-digit-width-idranges.owl",
                "invalid-no-prefix-name-idranges.owl",
                "invalid-policy-name-idranges.owl",
                "invalid-range-id-idranges.owl"
        };

        for ( String file : invalidFiles ) {
            Assertions.assertThrows(InvalidIDPolicyException.class, () -> p.read("src/test/resources/input/" + file));
        }
    }
}
