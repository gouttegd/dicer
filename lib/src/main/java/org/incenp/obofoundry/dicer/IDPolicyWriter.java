/*
 * Dicer - OBO ID range library
 * Copyright © 2024,2025 Damien Goutte-Gattat
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * A class to serialise an ID policy to file.
 */
public class IDPolicyWriter {

    private Writer writer;

    /**
     * Writes a policy to a file.
     * 
     * @param policy   The policy to write.
     * @param filename The name of the file to write the policy to.
     * @throws IOException If any I/O error occurs when attempting to write to the
     *                     file.
     */
    public void write(IDPolicy policy, String filename) throws IOException {
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
    public void write(IDPolicy policy, File file) throws IOException {
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
    public void write(IDPolicy policy, OutputStream stream) throws IOException {
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
    public void write(IDPolicy policy, Writer writer) throws IOException {
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

    private String getOntologyIRI(IDPolicy policy) {
        return String.format("%s/%s-idranges.owl", policy.getName(), policy.getPrefixName().toLowerCase());
    }

    private void writeLine(String line, Object... args) throws IOException {
        writer.append(String.format(line + "\n", args));
    }

    private void writePrefixDeclaration(String prefixName, String prefix) throws IOException {
        writer.append(String.format("Prefix: %s: <%s>\n", prefixName, prefix));
    }
}
