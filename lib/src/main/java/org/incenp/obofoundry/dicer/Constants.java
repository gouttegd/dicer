/*
 * Dicer - OBO ID range library
 * Copyright © 2024 Damien Goutte-Gattat
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

import org.semanticweb.owlapi.model.IRI;

/**
 * Constants used throughout the package.
 */
public class Constants {

    /**
     * The property that defines the IRI prefix that a policy is intended for.
     */
    public static final IRI IDPREFIX_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000599");

    /**
     * The property that defines the prefix name of a ID policy.
     */
    public static final IRI IDSFOR_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000598");

    /**
     * The property that defines the number of digits that IDs created according to
     * a given policy should have.
     */
    public static final IRI IDDIGITS_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000596");

    /**
     * The property that associates an ID range to a user name.
     */
    public static final IRI ALLOCATEDTO_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000597");
}
