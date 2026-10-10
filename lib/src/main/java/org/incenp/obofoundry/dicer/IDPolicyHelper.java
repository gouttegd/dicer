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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * A class of helper methods to work with ID policies.
 */
public class IDPolicyHelper {

    /**
     * Finds an ID policy file in the current directory. This methods looks for a
     * single file whose name ends with {@code -idranges.owl} in the current
     * directory.
     * 
     * @return An Optional of the ID policy file (empty if there is no such file).
     */
    public static Optional<String> findIDPolicyFile() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith("-idranges.owl");
            }
        };

        File cwd = new File(".");
        String[] files = cwd.list(filter);
        if ( files.length == 1 ) {
            return Optional.of(files[0]);
        }
        return Optional.empty();
    }

    /**
     * Gets the ID range allocated to the given user according to the ID policy.
     * 
     * @param user     The user for which to retrieve the range. May be
     *                 {@code null}, in which case the method will look for a range
     *                 allocated to any of the user names specified in
     *                 {@code defaults} instead.
     * @param defaults A list of default user names to use if {@code user} is
     *                 {@code null}.
     * @param filename The name of the file containing the ID policy; may be
     *                 {@code null}, in which case the method will search for a file
     *                 ending with {@code -idranges.owl} in the current directory.
     * @return The requested ID range (either the range allocated to {@code user} if
     *         not {@code null}, of the range allocated to any one of the
     *         {@code defaults} users).
     * @throws InvalidIDPolicyException If the file is not a valid ID policy file.
     * @throws IOException              If the policy file cannot be read for any
     *                                  reason (including a missing file).
     * @throws IDRangeNotFoundException If neither the requested range nor any of
     *                                  the default ranges can be found in the
     *                                  policy.
     */
    public static IDRange getRange(String user, String[] defaults, String filename)
            throws InvalidIDPolicyException, IOException, IDRangeNotFoundException {
        if ( user == null && defaults == null ) {
            throw new IllegalArgumentException("Both user and defaults cannot be null");
        }

        if ( filename == null ) {
            filename = findIDPolicyFile().orElseThrow(() -> new FileNotFoundException());
        }
        IDPolicy policy = new IDPolicyReader().read(filename);

        IDRange range = null;
        if ( user != null ) {
            range = policy.getRange(user);
        } else if ( defaults != null && defaults.length > 0 ) {
            range = policy.getAnyRange(Arrays.asList(defaults));
        }

        return range;
    }
}
