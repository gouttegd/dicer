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
