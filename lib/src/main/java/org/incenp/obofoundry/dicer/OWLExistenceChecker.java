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
import org.semanticweb.owlapi.model.OWLSignature;

/**
 * An IExistenceChecker implementation that uses a OWLSignature object as the
 * backend. That is, it will consider that a given ID exists / is in use if it
 * is present in the signature.
 */
public class OWLExistenceChecker implements IExistenceChecker {

    private OWLSignature signature;

    /**
     * Creates a new instance.
     * 
     * @param signature The OWLSignature (typically a OWLOntology) to use as source
     *                  of truth for the existence of a given identifier.
     */
    public OWLExistenceChecker(OWLSignature signature) {
        this.signature = signature;
    }

    @Override
    public boolean exists(String id) {
        return signature.containsEntityInSignature(IRI.create(id));
    }
}
