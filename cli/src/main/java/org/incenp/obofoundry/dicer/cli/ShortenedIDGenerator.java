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

package org.incenp.obofoundry.dicer.cli;

import org.incenp.obofoundry.dicer.IAutoIDGenerator;
import org.incenp.obofoundry.dicer.IDNotFoundException;

/**
 * An ID generator that produces OBO-style short-form IDs (“CURIEs”).
 * <p>
 * This generator wraps another generator to automatically shorten the IDs
 * produced by the wrapped generator. It assumes that the IDs produced are of
 * the form <code>PREFIX/XXXX_ZZZZ</code>, and will turn them into
 * <code>XXXX:ZZZZ</code>.
 */
public class ShortenedIDGenerator implements IAutoIDGenerator {

    private IAutoIDGenerator inner;

    /**
     * Creates a new instance.
     * 
     * @param innerGenerator The generator whose IDs should be shortened.
     */
    public ShortenedIDGenerator(IAutoIDGenerator innerGenerator) {
        inner = innerGenerator;
    }

    @Override
    public String nextID() throws IDNotFoundException {
        String next = inner.nextID();
        int pos = next.lastIndexOf('/');
        if ( pos != -1 ) {
            String suffix = next.substring(pos + 1);
            pos = suffix.indexOf('_');
            if ( pos != -1 ) {
                next = suffix.substring(0, pos) + ":" + suffix.substring(pos + 1);
            }
        }
        return next;
    }

}
