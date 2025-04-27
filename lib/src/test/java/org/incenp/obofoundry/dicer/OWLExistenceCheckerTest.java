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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLExistenceCheckerTest {
    private OWLOntology ontology;

    @BeforeEach
    private void createOntology() throws OWLOntologyCreationException {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        ontology = mgr.createOntology();
        OWLDataFactory fac = mgr.getOWLDataFactory();
        mgr.addAxiom(ontology, fac.getOWLDeclarationAxiom(fac.getOWLClass(IRI.create("https://example.org/0001"))));
        mgr.addAxiom(ontology,
                fac.getOWLDeclarationAxiom(fac.getOWLObjectProperty(IRI.create("https://example.org/0002"))));
    }

    @Test
    void testExistingIDs() {
        IExistenceChecker checker = new OWLExistenceChecker(ontology);
        Assertions.assertTrue(checker.exists("https://example.org/0001"));
        Assertions.assertTrue(checker.exists("https://example.org/0002"));
    }

    @Test
    void testUnusedIDs() {
        IExistenceChecker checker = new OWLExistenceChecker(ontology);
        Assertions.assertFalse(checker.exists("https://example.org/0003"));
    }
}
