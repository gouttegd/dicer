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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.incenp.obofoundry.dicer.IDPolicy;
import org.incenp.obofoundry.dicer.IDPolicyReader;
import org.incenp.obofoundry.dicer.IDPolicyWriter;
import org.incenp.obofoundry.dicer.IDRange;
import org.incenp.obofoundry.dicer.IDRangeNotFoundException;
import org.incenp.obofoundry.dicer.InvalidIDPolicyException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParserFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * A command to manipulate ID policy files.
 */
@Command(name = "policy",
        description = "Manipulate OBO Foundry-style ID policies.",
        optionListHeading = "%nGeneral options:%n",
        footer = "Report bugs to <dgouttegattat@incenp.org>.",
        footerHeading = "%n")
public class PolicyTool implements Runnable {

    @ParentCommand
    private SimpleCLI cli;

    @Option(names = { "-h", "--help" }, usageHelp = true,
            description = "Show this help message and exit.")
    private boolean showHelp;

    @ArgGroup(exclusive = false, multiplicity = "1", heading = "%nI/O options:%n")
    private IOOptions ioOptions = new IOOptions();

    private static class IOOptions {
        @Parameters(index = "0", paramLabel = "FILE",
                description = "The policy file to read.")
        String inputFile;

        @Option(names = { "--assume-manchester" },
                description = "Assume the policy file is in OWL Manchester syntax.")
        public boolean assumeManchester = false;

        @Option(names = { "--show-owlapi-error" },
                description = "Print the full OWLAPI error message if the policy cannot be loaded")
        public boolean showOWLAPIError = false;

        String outputFile;

        @Option(names = { "-o", "--output" },
                paramLabel = "FILE",
                description = "Write the policy to FILE. Default is to write back to the original input file.")
        public void setOutputFile(String file) {
            outputFile = file;
            forceWrite = true;
        }

        public String getOutputFile() {
            return outputFile != null ? outputFile : inputFile;
        }

        @Option(names = { "-s", "--save" }, defaultValue = "false",
                description = "Force writing the policy. Implied by --output and any option that modifies the policy.")
        boolean write;

        boolean forceWrite = false;

        boolean isWriteEnabled() {
            return forceWrite ? forceWrite : write;
        }
    }

    @ArgGroup(validate = false, heading = "%nEditing options:%n")
    private EditOptions editOptions = new EditOptions();

    private static class EditOptions {
        @Option(names = "--add-range",
                paramLabel = "USER",
                description = "Add a new range allocated to USER.")
        String newRange;

        @Option(names = "--size",
                paramLabel = "SIZE", defaultValue = "10000",
                description = "The size of the range to add (default: 10,000).")
        int size;
    }

    @ArgGroup(validate = false, heading = "%nListing options:%n")
    private ListOptions listOptions = new ListOptions();

    private static class ListOptions {
        @Option(names = { "-l", "--list" }, defaultValue = "false",
                description = "Print a list of the ranges")
        boolean showList;

        @Option(names = "--show-unallocated", defaultValue = "false",
                description = "When listing ranges, also show unallocated ranges.")
        boolean showUnallocated;

        @Option(names = "--min-size",
                paramLabel = "N", defaultValue = "10",
                description = "Do not show ranges smaller than N (default: 10; set to zero to show all ranges).")
        int minSize;
    }

    private IDPolicy policy = null;

    @Override
    public void run() {
        policy = readPolicy();

        if ( editOptions.newRange != null ) {
            try {
                IDRange rng = policy.addRange(editOptions.newRange, null, editOptions.size);
                cli.info("Allocated range [%d..%d) for user \"%s\"", rng.getLowerBound(), rng.getUpperBound(),
                        rng.getName());
                ioOptions.forceWrite = true;
            } catch ( IDRangeNotFoundException e ) {
                cli.error("Cannot allocate range: %s", e.getMessage());
            }
        }

        if ( listOptions.showList ) {
            List<IDRange> ranges = policy.getRangesByLowerBound();
            if ( listOptions.showUnallocated ) {
                ranges.addAll(policy.getUnallocatedRanges());
                ranges.sort((a, b) -> Integer.compare(a.getLowerBound(), b.getLowerBound()));
            }
            for ( IDRange rng : ranges ) {
                if ( rng.getSize() >= listOptions.minSize ) {
                    System.out.printf("%s: [%d..%d)\n", rng.getName(), rng.getLowerBound(), rng.getUpperBound());
                }
            }
        }

        if ( ioOptions.isWriteEnabled() ) {
            try {
                new IDPolicyWriter().write(policy, ioOptions.getOutputFile());
            } catch ( IOException e ) {
                cli.error("Cannot write policy file: %s", e.getMessage());
            }
        }
    }

    private IDPolicy readPolicy() {
        IDPolicy policy = null;
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();

        if ( ioOptions.assumeManchester ) {
            // Disable all parsers other than the Manchester parser; this is mostly intended
            // so that the error message in case of an invalid file is not "polluted" by the
            // errors reported by all the other parsers.
            mgr.setOntologyParsers(Set.of(new ManchesterOWLSyntaxOntologyParserFactory()));
        }
        try {
            OWLOntology ont = mgr.loadOntologyFromOntologyDocument(new File(ioOptions.inputFile));
            policy = new IDPolicyReader().fromOntology(ont);
        } catch ( OWLOntologyCreationException | OWLRuntimeException e ) {
            String error = "Cannot read policy file";
            if ( ioOptions.showOWLAPIError ) {
                // The OWLAPI error message is very verbose, so we only show it if the user
                // explicitly requested it.
                error += "\n" + e.getMessage();
            }
            cli.error(error);
        } catch ( InvalidIDPolicyException e ) {
            cli.error("Invalid ID range policy: %s", e.getMessage());
        }
        return policy;
    }
}
