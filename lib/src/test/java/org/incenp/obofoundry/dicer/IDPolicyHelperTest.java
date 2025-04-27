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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IDPolicyHelperTest {

    private final static String TEST_FILE = "src/test/resources/input/myont-idranges.owl";

    @Test
    void testFindOneIDPolicyFile() throws IOException {
        File tmpFile = new File("test-idranges.owl");
        FileUtils.copyFile(new File(TEST_FILE), tmpFile);

        String found = IDPolicyHelper.findIDPolicyFile().get();
        Assertions.assertEquals("test-idranges.owl", found);

        tmpFile.delete();
    }

    @Test
    void testFindOnlyOneFile() throws IOException {
        File srcFile = new File(TEST_FILE);
        File tmpFile1 = new File("test1-idranges.owl");
        File tmpFile2 = new File("test2-idranges.owl");
        FileUtils.copyFile(srcFile, tmpFile1);
        FileUtils.copyFile(srcFile, tmpFile2);

        Assertions.assertFalse(IDPolicyHelper.findIDPolicyFile().isPresent());

        tmpFile1.delete();
        tmpFile2.delete();
    }

    @Test
    void testGetRangeFailsIfMissingFile() {
        Assertions.assertThrows(FileNotFoundException.class, () -> IDPolicyHelper.getRange("user1", null, null));
    }

    @Test
    void testGetRequestedRange() {
        try {
            IDRange rng = IDPolicyHelper.getRange("user1", null, TEST_FILE);
            Assertions.assertEquals("user1", rng.getName());
        } catch ( IOException | IDException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testGetDefaultRange() {
        try {
            IDRange rng = IDPolicyHelper.getRange(null, new String[] { "user3", "user2" }, TEST_FILE);
            Assertions.assertEquals("user2", rng.getName());
        } catch ( IOException | IDException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testGetRequestedRangeOverDefaults() {
        try {
            IDRange rng = IDPolicyHelper.getRange("user2", new String[] { "user1", "user3" }, TEST_FILE);
            Assertions.assertEquals("user2", rng.getName());
        } catch ( IOException | IDException e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testGetRangeFailsIfRequestedRangeDoesNotExist() {
        Assertions.assertThrows(IDRangeNotFoundException.class,
                () -> IDPolicyHelper.getRange("Alice", new String[] { "user1", "user3" }, TEST_FILE));
    }

    @Test
    void testGetRangeFailsIfDefaultRangesDoNotExist() {
        Assertions.assertThrows(IDRangeNotFoundException.class,
                () -> IDPolicyHelper.getRange(null, new String[] { "user3", "user4" }, TEST_FILE));
    }
}
