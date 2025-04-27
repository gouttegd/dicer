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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * A command to inject IDs into a TSV/CSV file.
 */
@Command(name = "tsv",
         description = "Inject generated IDs into a TSV file.",
         optionListHeading = "%nGeneral options:%n",
         footer = "Report bugs to <dgouttegattat@incenp.org>.",
         footerHeading = "%n")
public class TSVTool implements Runnable, ITSVListener {

    @ParentCommand
    private SimpleCLI cli;

    @Option(names = { "-h", "--help" }, usageHelp = true,
            description = "Show this help message and exit.")
    private boolean showHelp;

    @ArgGroup(validate = false, heading = "%nInput options:%n")
    private InputOptions inputOpts = new InputOptions();

    private static class InputOptions {

        @Parameters(index = "0", paramLabel = "FILE",
                    description = "The TSV file to inject IDs into.")
        private String file;

        @Option(names = "--input-sep", paramLabel = "SEP",
                description = "Column separator in input file. Allowed values: ${COMPLETION-CANDIDATES}. Default is AUTO.")
        private SeparatorMode separatorMode = SeparatorMode.AUTO;
    }

    @ArgGroup(validate = false, heading = "%nOutput options:%n")
    private OutputOptions outputOpts = new OutputOptions();

    private static class OutputOptions {

        @Option(names = { "-o", "--output" },
                paramLabel = "FILE",
                defaultValue = "-",
                description = "Write the result to FILE instead of standard output.")
        private String file;

        @Option(names = "--output-sep", paramLabel = "SEP",
                description = "Column separator in output file. Allowed values: ${COMPLETION-CANDIDATES}. Default is AUTO.")
        private SeparatorMode separatorMode = SeparatorMode.AUTO;
    }

    private PrintStream output;
    private String outputSep;

    enum SeparatorMode {
        TAB('\t'),
        COMMA(','),
        COLON(':'),
        SEMICOLON(';'),
        AUTO(-1);

        int separator;

        SeparatorMode(int separator) {
            this.separator = separator;
        }
    }

    @Override
    public void run() {

        if ( outputOpts.file.equals("-") ) {
            output = System.out;
        } else {
            try {
                output = new PrintStream(outputOpts.file);
            } catch ( FileNotFoundException e ) {
                cli.error("Cannot write to %s: %s", outputOpts.file, e.getMessage());
            }
        }

        try {
            TSVReader reader = new TSVReader(inputOpts.file);
            reader.addListener(this);
            reader.setSeparator(inputOpts.separatorMode.separator);
            reader.read();
            output.close();
        } catch ( IOException e ) {
            cli.error("Cannot read %s: %s", inputOpts.file, e.getMessage());
        }
    }

    @Override
    public void onComment(String comment) {
        output.append('#');
        output.append(comment);
        output.append('\n');
    }

    @Override
    public void onHeader(List<String> header, char separator) {
        if ( outputOpts.separatorMode == SeparatorMode.AUTO ) {
            outputSep = Character.toString(separator);
        } else {
            outputSep = Character.toString((char) outputOpts.separatorMode.separator);
        }
        output.append(String.join(outputSep, header));
        output.append('\n');
    }

    @Override
    public void onRow(List<String> row) {
        output.append(String.join(outputSep, row));
        output.append('\n');
    }

}
