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

    /**
     * Creates a new instance from a IDRange object.
     * 
     * @param range   The ID policy range for which to generate IDs.
     * @param checker An object to check whether a given ID already exists; the
     *                generator will call it to avoid generating IDs that are
     *                already in use.
     */
    public SequentialIDGenerator(IDRange range, IExistenceChecker checker) {
        this(range.getFormat(), range.getLowerBound(), range.getUpperBound(), checker);
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
