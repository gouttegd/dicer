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
