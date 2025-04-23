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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLSignature;

/**
 * Generates numerical IDs within a given range. This class is similar to
 * {@link SequentialIDGenerator} except that numerical IDs are chosen randomly
 * within the target range, instead of being chosen sequentially.
 */
public class RandomizedIDGenerator implements IAutoIDGenerator {

    private OWLSignature signature;
    private String format;
    private int lowerBound;
    private int upperBound;
    private boolean lowerBoundFound = false;
    private Random rand = new Random();
    private HashSet<String> testedIDs = new HashSet<String>();

    /**
     * Creates a new instance.
     * 
     * @param signature THE OWLSignature to generate IDs for. The signature will be
     *                  checked to ensure the generated IDs do not clash with the
     *                  IDs of existing entities.
     * @param format    The format of newly generated IDs. It must contain a C-style
     *                  format specifier indicating where the numerical portion of
     *                  the ID should appear and in which format.
     * @param min       The lower bound (inclusive) for newly generated IDs.
     * @param max       The upper bound (exclusive) for newly generated IDs.
     */
    public RandomizedIDGenerator(OWLSignature signature, String format, int min, int max) {
        if ( min < 0 || max <= min ) {
            throw new IllegalArgumentException("Invalid range");
        }

        this.signature = signature;
        this.format = format;
        lowerBound = min;
        upperBound = max;
    }

    /**
     * Creates a new instance from the specified range object.
     * 
     * @param signature The OWLSignature to generate IDs for. The signature will be
     *                  checked to ensure the generated IDs do not clash with the
     *                  IDs of existing entities.
     * @param format    The format of newly generated IDs. It must contain a C-style
     *                  format specifier indicating where the numerical portion of
     *                  the ID should appear and in which format.
     * @param range     The range in which newly generated IDs should be picked.
     */
    public RandomizedIDGenerator(OWLSignature signature, String format, IDRange range) {
        this.signature = signature;
        this.format = format;
        lowerBound = range.getLowerBound();
        upperBound = range.getUpperBound();
    }

    /**
     * Creates a new instance from the specified ID range policy.
     * 
     * @param signature The OWLSignature to generate IDs for. The signature will be
     *                  checked to ensure the generated IDs do not clash with the
     *                  IDs of existing entities.
     * @param policy    The ID policy to use.
     * @param rangeName The name of the range within the given policy to use.
     * @throws IDRangeNotFoundException If the policy does not contain any range
     *                                  associated with the specified name.
     */
    public RandomizedIDGenerator(OWLSignature signature, IDPolicy policy, String rangeName)
            throws IDRangeNotFoundException {
        this(signature, policy.getFormat(), policy.getRange(rangeName));
    }

    @Override
    public String nextID() throws OutOfIDSpaceException {
        // Find the lowest unused ID within the range, so that we can start testing
        // random IDs from there.
        while ( !lowerBoundFound && lowerBound < upperBound ) {
            String test = String.format(format, lowerBound);
            if ( !exists(test, false) ) {
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
            found = !exists(id, true);
        } while ( i < upperBound && !found );

        if ( i >= upperBound ) {
            throw new OutOfIDSpaceException("No available ID in range");
        }

        return id;
    }

    /*
     * Tests whether an ID is already in use, and cache the result so that we do not
     * have to query the signature repeatedly for the same ID.
     * 
     * If 'add' is true and the ID is not already in use, add it to the cache so
     * that it can be marked as now being in use.
     */
    private boolean exists(String id, boolean add) {
        if ( testedIDs.contains(id) ) {
            return true;
        } else if ( signature.containsEntityInSignature(IRI.create(id)) ) {
            testedIDs.add(id);
            return true;
        } else if ( add ) {
            testedIDs.add(id);
        }
        return false;
    }
}
