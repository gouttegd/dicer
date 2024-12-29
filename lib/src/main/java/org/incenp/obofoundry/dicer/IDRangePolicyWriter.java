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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * A class to serialise an ID range policy to file.
 */
public class IDRangePolicyWriter {

    private Writer writer;

    /**
     * Writes a policy to a file.
     * 
     * @param policy   The policy to write.
     * @param filename The name of the file to write the policy to.
     * @throws IOException If any I/O error occurs when attempting to write to the
     *                     file.
     */
    public void write(IDRangePolicy policy, String filename) throws IOException {
        write(policy, new FileWriter(filename));
        writer.close();
    }

    /**
     * Writes a policy to a file.
     * 
     * @param policy The policy to write.
     * @param file   The file to write the policy to.
     * @throws IOException If any I/O error occurs when attempting to write to the
     *                     file.
     */
    public void write(IDRangePolicy policy, File file) throws IOException {
        write(policy, new FileWriter(file));
        writer.close();
    }

    /**
     * Writes a policy to a stream.
     * 
     * @param policy The policy to write.
     * @param stream The stream to write the policy to.
     * @throws IOException If any I/O error occurs when attempting to write to the
     *                     stream.
     */
    public void write(IDRangePolicy policy, OutputStream stream) throws IOException {
        write(policy, new OutputStreamWriter(stream));
    }

    /**
     * Writes a policy to a Writer object.
     * 
     * @param policy The policy to write.
     * @param writer The writer object to write the policy to.
     * @throws IOException If any I/O error occurs when attempting to write to the
     *                     writer object.
     */
    public void write(IDRangePolicy policy, Writer writer) throws IOException {
        this.writer = writer;

        /*
         * ID policy files are expected to be somewhat human-readable, and it's easier
         * to achieve if we write exactly what we want, rather than relying on the
         * OWLAPI serialisers.
         */
        writePrefixDeclaration("idrange", policy.getName() + "/idrange/");
        writePrefixDeclaration("allocatedto", Constants.ALLOCATEDTO_IRI.toString());
        writePrefixDeclaration("iddigits", Constants.IDDIGITS_IRI.toString());
        writePrefixDeclaration("idprefix", Constants.IDPREFIX_IRI.toString());
        writePrefixDeclaration("idsfor", Constants.IDSFOR_IRI.toString());
        writePrefixDeclaration("comment", OWLRDFVocabulary.RDFS_COMMENT.toString());
        writeLine("");
        writeLine("Ontology: <%s>", getOntologyIRI(policy));
        writeLine("");
        writeLine("Annotations:");
        writeLine("    idprefix: \"%s\",", policy.getPrefix());
        writeLine("    iddigits: %d,", policy.getWidth());
        writeLine("    idsfor: \"%s\"", policy.getPrefixName());
        writeLine("");
        writeLine("AnnotationProperty: allocatedto:\n");
        writeLine("AnnotationProperty: idprefix:\n");
        writeLine("AnnotationProperty: iddigits:\n");
        writeLine("AnnotationProperty: idsfor:\n");
        writeLine("AnnotationProperty: comment:");

        for ( IDRange rng : policy.getRangesByID() ) {
            boolean hasComment = rng.getComment() != null;
            writeLine("\nDatatype: idrange:%d", rng.getID());
            writeLine("    Annotations:");
            writeLine("        allocatedto: \"%s\"%s", rng.getName(), hasComment ? "," : "");
            if ( hasComment ) {
                writeLine("        comment: \"%s\"", rng.getComment());
            }
            writeLine("    EquivalentTo:");
            writeLine("        xsd:integer[>= %d, < %d]", rng.getLowerBound(), rng.getUpperBound());
        }

        writer.flush();
    }

    private String getOntologyIRI(IDRangePolicy policy) {
        return String.format("%s/%s-idranges.owl", policy.getName(), policy.getPrefixName().toLowerCase());
    }

    private void writeLine(String line, Object... args) throws IOException {
        writer.append(String.format(line + "\n", args));
    }

    private void writePrefixDeclaration(String prefixName, String prefix) throws IOException {
        writer.append(String.format("Prefix: %s: <%s>\n", prefixName, prefix));
    }
}
