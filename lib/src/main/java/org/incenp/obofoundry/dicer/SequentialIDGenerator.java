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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSignature;

/**
 * Generates numerical IDs sequentially within a given range.
 */
public class SequentialIDGenerator implements IAutoIDGenerator {

    private OWLSignature signature;
    private String format;
    private int lowerBound;
    private int upperBound;

    /**
     * Creates a new instance.
     * 
     * @param signature The OWLSignature to generate IDs for. The signature will be
     *                  checked to ensure the generated IDs do not clash with the
     *                  IDs of existing entities.
     * @param format    The format of newly generated IDs. It must contain a C-style
     *                  format specifier indicating where the numerical portion of
     *                  the ID should appear and in which format.
     * @param min       The lower bound (inclusive) for newly generated IDs.
     * @param max       The upper bound (exclusive) for newly generated IDs.
     */
    public SequentialIDGenerator(OWLSignature signature, String format, int min, int max) {
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
    public SequentialIDGenerator(OWLSignature signature, String format, IDRange range) {
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
     * @param policy    The ID range policy to use.
     * @param rangeName The name of the range within the given policy to use.
     * @throws NoSuchIDRangeException If the policy does not contain any range
     *                                associated with the specified name.
     */
    public SequentialIDGenerator(OWLOntology signature, IDRangePolicy policy, String rangeName)
            throws NoSuchIDRangeException {
        this(signature, policy.getFormat(), policy.getRange(rangeName));
    }

    @Override
    public String nextID() throws OutOfIDSpaceException {
        while ( lowerBound < upperBound ) {
            String id = String.format(format, lowerBound++);
            if ( !signature.containsEntityInSignature(IRI.create(id)) ) {
                return id;
            }
        }
        throw new OutOfIDSpaceException("No available ID in range");
    }
}
