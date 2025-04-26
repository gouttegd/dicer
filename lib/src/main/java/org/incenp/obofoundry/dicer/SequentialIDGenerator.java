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

/**
 * Generates numerical IDs sequentially within a given range.
 */
public class SequentialIDGenerator implements IAutoIDGenerator {

    private String format;
    private int lowerBound;
    private int upperBound;
    private IExistenceChecker checker;

    /**
     * Creates a new instance.
     * 
     * @param format  The format of newly generated IDs. It must contain a C-style
     *                format specified where and how the numerical portion of the ID
     *                should appear.
     * @param min     The lower bound (inclusive) for newly generated IDs.
     * @param max     The upper bound (exclusive) for newly generated IDs.
     * @param checker An object to check whether a given ID already exists; the
     *                generator will call it to avoid generating IDs that are
     *                already in use.
     */
    public SequentialIDGenerator(String format, int min, int max, IExistenceChecker checker) {
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
        while ( lowerBound < upperBound ) {
            String id = String.format(format, lowerBound++);
            if ( !checker.exists(id) ) {
                return id;
            }
        }
        throw new IDNotFoundException("No available ID in range");
    }
}
