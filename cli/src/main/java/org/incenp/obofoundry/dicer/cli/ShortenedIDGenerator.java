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
