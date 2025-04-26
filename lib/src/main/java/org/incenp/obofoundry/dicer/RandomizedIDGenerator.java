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

import java.util.HashSet;
import java.util.Random;

/**
 * Generates numerical IDs within a given range. This class is similar to
 * {@link SequentialIDGenerator} except that numerical IDs are chosen randomly
 * within the target range, instead of being chosen sequentially.
 */
public class RandomizedIDGenerator implements IAutoIDGenerator {

    private String format;
    private int lowerBound;
    private int upperBound;
    private boolean lowerBoundFound = false;
    private Random rand = new Random();
    private IExistenceChecker checker;
    private HashSet<String> generatedIDs = new HashSet<>();

    /**
     * Creates a new instance.
     * 
     * @param format  The format of newly generated IDs. It must contain a C-style
     *                format specifier indicating where and how the numerical
     *                portion of the ID should appear.
     * @param min     The lower bound (inclusive) for newly generated IDs.
     * @param max     The upper bound (exclusive) for newly generated IDs.
     * @param checker An object to check whether a given ID already exists; the
     *                generator will call it to avoid generating IDs that are
     *                already in use.
     */
    public RandomizedIDGenerator(String format, int min, int max, IExistenceChecker checker) {
        if ( min < 0 || max <= min ) {
            throw new IllegalArgumentException("Invalid range");
        }
        this.format = format;
        this.checker = checker;
        lowerBound = min;
        upperBound = max;
    }

    @Override
    public String nextID() throws IDNotFoundException {
        // Find the lowest unused ID within the range, so that we can start testing
        // random IDs from there.
        while ( !lowerBoundFound && lowerBound < upperBound ) {
            String test = String.format(format, lowerBound);
            if ( !checker.exists(test) ) {
                lowerBoundFound = true;
            } else {
                lowerBound += 1;
            }
        }

        boolean found = false;
        int i = lowerBound;
        String id = null;
        do {
            i += rand.nextInt(100);
            id = String.format(format, i);
            found = !checker.exists(id) && !generatedIDs.contains(id);
        } while ( i < upperBound && !found );

        if ( i >= upperBound ) {
            throw new IDNotFoundException("No available ID in range");
        }

        generatedIDs.add(id);
        return id;
    }
}
