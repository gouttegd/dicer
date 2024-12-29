/*
 * Dicer - OBO ID range library
 * Copyright © 2024 Damien Goutte-Gattat
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
