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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDPolicyWriterTest {

    @Test
    void testSimpleWrite() throws IOException {
        IDPolicy policy = new IDPolicy("myont");
        try {
            policy.addRange("user1", null, 10000);
            policy.addRange("user2", "Range for user 2", 20000);
        } catch ( IDRangeNotFoundException e ) {
            Assertions.fail(e);
        }

        assertWrittenAsExpected(policy, "myont", null);
    }

    private void assertWrittenAsExpected(IDPolicy policy, String expectedBasename, String actualBasename)
            throws IOException {
        if ( actualBasename == null ) {
            actualBasename = expectedBasename;
        }

        File written = new File("src/test/resources/output/" + actualBasename + "-idranges.owl.out");
        written.getParentFile().mkdir();
        IDPolicyWriter writer = new IDPolicyWriter();
        writer.write(policy, written);

        File expected = new File("src/test/resources/output/" + expectedBasename + "-idranges.owl");
        if ( !expected.exists() ) {
            expected = new File("src/test/resources/input/" + expectedBasename + "-idranges.owl");
        }

        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }
}
