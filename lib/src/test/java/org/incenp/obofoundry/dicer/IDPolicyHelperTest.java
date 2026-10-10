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
