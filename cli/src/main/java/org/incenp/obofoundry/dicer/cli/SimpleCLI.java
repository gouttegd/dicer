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

package org.incenp.obofoundry.dicer.cli;

import java.io.IOException;
import java.util.List;

import org.incenp.obofoundry.dicer.IDRange;
import org.incenp.obofoundry.dicer.IDRangePolicy;
import org.incenp.obofoundry.dicer.IDRangePolicyReader;
import org.incenp.obofoundry.dicer.IDRangePolicyWriter;
import org.incenp.obofoundry.dicer.InvalidIDRangePolicyException;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A command-line interface to manipulate ID range policies.
 */
@Command(name = "dicer-cli",
         mixinStandardHelpOptions = true,
         versionProvider = CommandHelper.class,
         description = "Manipulate OBO Foundry-style ID range policies.",
         footer = "Report bugs to <dgouttegattat@incenp.org>.",
         optionListHeading = "%nGeneral options:%n",
         footerHeading = "%n")
public class SimpleCLI implements Runnable {

    @ArgGroup(validate = false, heading = "%nI/O options:%n")
    private IOOptions ioOptions = new IOOptions();

    private static class IOOptions {
        @Option(names = {"-i", "--input"},
                paramLabel = "FILE",
                description = "Load the policy from FILE.")
        String inputFile;

        @Option(names = { "-o", "--output"},
                paramLabel = "FILE",
                description = "Write the policy to FILE.")
        String outputFile;
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
                description = "The size of the range to add (default: 10000).")
        int size;
    }

    @ArgGroup(validate = false, heading = "%nListing options:%n")
    private ListOptions listOptions = new ListOptions();

    private static class ListOptions {
        @Option(names = { "-l", "--list" },
                defaultValue = "false",
                description = "Print a list of the ranges.")
        boolean showList;

        @Option(names = "--show-unallocated", defaultValue = "false",
                description = "When listing ranges, also show unallocated ranges.")
        boolean showUnallocated;

        @Option(names = "--min-size", paramLabel = "N", defaultValue = "10",
                description = "Do not show ranges smaller than N (default: 10; set to zero to show all ranges).")
        int minSize;
    }

    private CommandHelper helper = new CommandHelper();
    private IDRangePolicy policy = null;

    public static void main(String[] args) {
        System.exit(run(args));
    }

    /*
     * The real entry point. It is separate from the main method so that it can be
     * called from the test suite without terminating the testing process.
     */
    public static int run(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        int rc = new picocli.CommandLine(cli).setExecutionExceptionHandler(cli.helper)
                .setCaseInsensitiveEnumValuesAllowed(true).setUsageHelpLongOptionsMaxWidth(23)
                .setUsageHelpAutoWidth(true).execute(args);
        return rc;
    }

    @Override
    public void run() {
        if ( ioOptions.inputFile != null ) {
            IDRangePolicyReader reader = new IDRangePolicyReader();
            try {
                policy = reader.read(ioOptions.inputFile);
            } catch ( IOException | InvalidIDRangePolicyException e ) {
                helper.error("Cannot read policy file: %s", e.getMessage());
            }
        }

        if ( policy == null ) {
            return;
        }

        if ( editOptions.newRange != null ) {
            IDRange rng = policy.addRange(editOptions.newRange, null, editOptions.size);
            if ( rng == null ) {
                helper.error("Cannot allocate a range of %d IDs", editOptions.size);
            }

            helper.info("Allocated range [%d..%d) for user \"%s\"", rng.getLowerBound(), rng.getUpperBound(),
                    rng.getName());
        }

        if ( listOptions.showList ) {
            List<IDRange> ranges = policy.getRangesByLowerBound();
            if ( listOptions.showUnallocated ) {
                ranges.addAll(policy.getUnallocatedRanges());
                ranges.sort((a, b) -> Integer.compare(a.getLowerBound(), b.getLowerBound()));
            }
            for ( IDRange rng : ranges ) {
                printRange(rng);
            }
        }

        if ( ioOptions.outputFile != null ) {
            try {
                new IDRangePolicyWriter().write(policy, ioOptions.outputFile);
            } catch ( IOException e ) {
                helper.error("Cannot write policy file: %s", e.getMessage());
            }
        }
    }

    private void printRange(IDRange range) {
        if ( range.getSize() < listOptions.minSize ) {
            return;
        }
        System.out.printf("%s: [%d..%d)\n", range.getName(), range.getLowerBound(), range.getUpperBound());
    }
}
