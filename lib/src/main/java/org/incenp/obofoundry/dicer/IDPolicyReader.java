/*
 * Dicer - OBO ID range library
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.util.OWLDataVisitorExAdapter;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * A class to read an ID policy from an ontology file.
 */
public class IDPolicyReader {

    private RangeDatatypeVisitor visitor = new RangeDatatypeVisitor();

    /**
     * Parses the given file into an ID policy.
     * 
     * @param filename The name of the file to parse.
     * @return The policy read from the file.
     * @throws IOException              If any I/O error occurs when trying to read
     *                                  the file.
     * @throws InvalidIDPolicyException If the file does not contain a valid policy.
     */
    public IDPolicy read(String filename) throws IOException, InvalidIDPolicyException {
        OWLOntology ont = null;
        try {
            ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(filename));
        } catch ( OWLOntologyCreationIOException e ) {
            throw new IOException("Cannot load ID range policy", e);
        } catch ( OWLOntologyCreationException | OWLRuntimeException e ) {
            throw new InvalidIDPolicyException("Cannot load ID range policy", e);
        }

        return fromOntology(ont);
    }

    /**
     * Extracts an ID policy from the given ontology.
     * 
     * @param ontology An ontology that describes the ID policy.
     * @return The corresponding policy.
     * @throws InvalidIDPolicyException If the ontology does not describe a valid
     *                                  policy.
     */
    public IDPolicy fromOntology(OWLOntology ontology) throws InvalidIDPolicyException {
        String name = getPolicyName(ontology.getOntologyID().getOntologyIRI().orNull());
        String prefix = null;
        String prefixName = null;
        int width = 7;

        for ( OWLAnnotation annots : ontology.getAnnotations() ) {
            OWLAnnotationValue value = annots.getValue();
            if ( !value.isLiteral() ) {
                continue;
            }

            IRI property = annots.getProperty().getIRI();
            if ( property.equals(Constants.IDPREFIX_IRI) ) {
                prefix = value.asLiteral().get().getLiteral();
            } else if ( property.equals(Constants.IDSFOR_IRI) ) {
                prefixName = value.asLiteral().get().getLiteral();
            } else if ( property.equals(Constants.IDDIGITS_IRI) ) {
                try {
                    width = value.asLiteral().get().parseInteger();
                } catch ( NumberFormatException e ) {
                    throw new InvalidIDPolicyException("Invalid ID width: %s",
                            value.asLiteral().get().getLiteral());
                }
            }
        }
        if ( prefix == null ) {
            throw new InvalidIDPolicyException("Missing IRI prefix");
        }
        if ( prefixName == null ) {
            throw new InvalidIDPolicyException("Missing prefix name");
        }

        IDPolicy policy = new IDPolicy(name, prefix, prefixName, width);
        for ( OWLDatatype datatype : ontology.getDatatypesInSignature() ) {
            rangeFromDatatype(policy, ontology, datatype);
        }

        return policy;
    }

    /*
     * Gets the policy name from an ontology IRI.
     */
    private String getPolicyName(IRI ontologyIRI) throws InvalidIDPolicyException {
        if ( ontologyIRI == null ) {
            throw new InvalidIDPolicyException("Missing policy name");
        }
        String s = ontologyIRI.toString();
        int lastSlash = s.lastIndexOf('/');
        if ( !s.endsWith("-idranges.owl") || lastSlash == -1 ) {
            throw new InvalidIDPolicyException("Invalid policy name: %s", s);
        }

        return s.substring(0, lastSlash);
    }

    /*
     * Parses a datatype definition into a range.
     */
    private void rangeFromDatatype(IDPolicy policy, OWLOntology ontology, OWLDatatype datatype)
            throws InvalidIDPolicyException {
        String name = null;
        String comment = null;

        for ( OWLAnnotationAssertionAxiom ax : ontology.getAnnotationAssertionAxioms(datatype.getIRI()) ) {
            if ( !ax.getValue().isLiteral() ) {
                continue;
            }
            if ( ax.getProperty().getIRI().equals(Constants.ALLOCATEDTO_IRI) ) {
                name = ax.getValue().asLiteral().get().getLiteral();
            } else if ( ax.getProperty().isComment() ) {
                comment = ax.getValue().asLiteral().get().getLiteral();
            }
        }

        if ( name != null ) {
            int id = getRangeID(datatype.getIRI().toString());
            for ( OWLDatatypeDefinitionAxiom ax : ontology.getDatatypeDefinitions(datatype) ) {
                ax.getDataRange().accept(visitor);
                if ( visitor.lower >= 0 ) {
                    policy.addRange(id, name, comment, visitor.lower, visitor.upper);
                }
            }
        }
    }

    /*
     * Extracts the numerical ID of a range from the datatype IRI.
     */
    private int getRangeID(String iri) throws InvalidIDPolicyException {
        int lastSlash = iri.lastIndexOf('/');
        if ( lastSlash == -1 ) {
            throw new InvalidIDPolicyException("Invalid range ID: %s", iri);
        }
        try {
            return Integer.parseInt(iri.substring(lastSlash + 1));
        } catch ( NumberFormatException e ) {
            throw new InvalidIDPolicyException("Invalid range ID: %s", iri);
        }
    }

    private class RangeDatatypeVisitor extends OWLDataVisitorExAdapter<Void> {

        private int lower = -1;
        private int upper = -1;

        public RangeDatatypeVisitor() {
            super(null);
        }

        @Override
        public Void visit(OWLDatatypeRestriction node) {
            lower = -1;
            upper = -1;

            for ( OWLFacetRestriction restriction : node.getFacetRestrictions() ) {
                if ( restriction.getFacetValue().isInteger() ) {
                    int value = restriction.getFacetValue().parseInteger();

                    OWLFacet facet = restriction.getFacet();
                    switch ( facet ) {
                    case MAX_EXCLUSIVE:
                        upper = value;
                        break;
                    case MAX_INCLUSIVE:
                        upper = value + 1;
                        break;
                    case MIN_EXCLUSIVE:
                        lower = value + 1;
                        break;
                    case MIN_INCLUSIVE:
                        lower = value;
                        break;
                    default:
                        break;

                    }
                }
            }

            return null;
        }
    }
}
