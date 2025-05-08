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
