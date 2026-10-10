/*
 * Dicer - OBO ID range library
 * Copyright © 2025,2026 Damien Goutte-Gattat
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

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TSVToolTest extends CLITestBase {

    @Test
    void testCommandLineRange() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000"
        });
    }

    @Test
    void testShortenedIDs() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-short-ids.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--shorten-id"
        });
    }

    @Test
    void testColumnIndex() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-column-2.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--column", "2"
        });
    }

    @Test
    void testColumnName() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-column-label.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--column", "label"
        });
    }

    @Test
    void testInvalidColumnIndex() throws IOException {
        runCommand(1, "sample.tsv", null, new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--column", "4"
        });
    }

    @Test
    void testInvalidColumnName() throws IOException {
        runCommand(1, "sample.tsv", null, new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--column", "inexisting column"
        });
    }

    @Test
    void testUsingExplicitIDPolicyFile() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-user2-policy.tsv", new String[] {
                "--policy", "../lib/src/test/resources/input/myont-idranges.owl",
                "--range", "user2"
        });
    }

    @Test
    void testUsingOntologyAsIDSource() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-checked-against-ontology.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--ontology", "src/test/resources/tsv/in-use.ttl"
        });
    }

    @Test
    void testUsingExplicitCatalog() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-checked-against-ontology.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--ontology", "src/test/resources/tsv/in-use-imports.ttl",
                "--catalog", "src/test/resources/tsv/catalog.xml"
        });
    }

    @Test
    void testDisablingCatalog() throws IOException {
        runCommand(1, "sample.tsv", null, new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--ontology", "src/test/resources/tsv/in-use-imports.ttl",
                "--catalog", "none"
        });
    }

    @Test
    void testOverwriteExistingValues() throws IOException {
        runCommand(0, "sample-existing-values.tsv", "sample-overwritten-values.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000"
        });
    }

    @Test
    void testDontOverwriteExistingValues() throws IOException {
        runCommand(0, "sample-existing-values.tsv", "sample-preserved-values.tsv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--no-overwrite"
        });
    }

    @Test
    void testOverwriteOnlyValuesWithGivenPrefix() throws IOException {
        runCommand(0, "sample-existing-values.tsv", "sample-preserved-prefixed-values.tsv",
                new String[] {
                        "--prefix", "https://example.org/DICER_",
                        "--min-id", "1000",
                        "--overwrite-prefix",
                        "https://example.org/DICER_00008"
        });
    }

    @Test
    void testExplicitInputSeparator() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-comma-input-sep.csv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--input-sep", "COMMA"
        });
    }

    @Test
    void testOutputSeparator() throws IOException {
        runCommand(0, "sample.tsv", "sample-edit1-comma-output-sep.csv", new String[] {
                "--prefix", "https://example.org/DICER_",
                "--min-id", "1000",
                "--output-sep", "COMMA"
        });
    }

    protected String getCommand() {
        return "tsv";
    }
}
