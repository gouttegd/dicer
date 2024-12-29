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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDRangePolicyReaderTest {

    @Test
    void testReadUberonPolicy() {
        IDRangePolicyReader p = new IDRangePolicyReader();
        IDRangePolicy policy = null;
        try {
            policy = p.read("src/test/resources/input/uberon-idranges.owl");
        } catch ( IOException e ) {
            Assertions.fail(e);
        } catch ( InvalidIDRangePolicyException e ) {
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
}
