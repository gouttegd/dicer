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

import org.incenp.obofoundry.dicer.IAutoIDGenerator;
import org.incenp.obofoundry.dicer.IDNotFoundException;
import org.incenp.obofoundry.dicer.IDPolicyHelper;
import org.incenp.obofoundry.dicer.IDRange;
import org.incenp.obofoundry.dicer.IDRangeNotFoundException;
import org.incenp.obofoundry.dicer.InvalidIDPolicyException;
import org.incenp.obofoundry.dicer.SequentialIDGenerator;

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

    @ArgGroup(validate = false, heading = "%nID generation options:%n")
    private IDGenOptions idGenOpts = new IDGenOptions();

    private static class IDGenOptions {

        @Option(names = { "-p", "--prefix" }, paramLabel = "PREFIX",
                description = "Prefix of IDs to generate.")
        private String prefix;

        @Option(names = { "-w", "--width" }, paramLabel = "NUM",
                description = "Number of digits in generated IDs (default: 7).")
        private int width = 7;

        @Option(names = { "-m", "--min-id" }, paramLabel = "NUM",
                description = "Smallest ID to generate.")
        private int min = -1;

        @Option(names = { "-M", "--max-id" }, paramLabel = "NUM",
                description = "Largest ID to generate (default: --min-id + 1000).")
        private int max = -1;

        @Option(names = { "-P", "--policy" }, paramLabel = "FILE",
                description = "Use ID policy in specified file.")
        private String policy;

        @Option(names = { "-r", "--range" }, paramLabel = "USER",
                description = "Use ID range allocated to specified user.")
        private String range;

        @Option(names = { "-s", "--shorten-id" }, defaultValue = "false",
                description = "Generate OBO-style short-form IDs.")
        private boolean shortFormat = false;
    }

    @ArgGroup(validate = false, heading = "%nEditing options:%n")
    private EditOptions editOpts = new EditOptions();

    private static class EditOptions {
        @Option(names = { "-c", "--column" }, paramLabel = "COL",
                description = "Name or 1-based index of the column in which to inject IDs.")
        private String columnName;

        @Option(names = { "--overwrite" }, negatable = true,
                defaultValue = "true", fallbackValue = "true",
                description = "Overwrite existing values. This is enabled by default.")
        private boolean overwrite;
    }

    private PrintStream output;
    private String outputSep;
    private IAutoIDGenerator generator;
    private int columnIndex;

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

        generator = getIDGenerator();

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

        if ( editOpts.columnName == null ) {
            columnIndex = 0;
        } else {
            try {
                columnIndex = Integer.parseUnsignedInt(editOpts.columnName) - 1;
            } catch ( NumberFormatException e ) {
                columnIndex = 0;
                for ( String name : header ) {
                    if ( name.equals(editOpts.columnName) ) {
                        break;
                    }
                    columnIndex += 1;
                }
            }
            if ( columnIndex < 0 || columnIndex >= header.size() ) {
                cli.error("Invalid column name or index: %s", editOpts.columnName);
            }
        }
    }

    @Override
    public void onRow(List<String> row) {
        if ( editOpts.overwrite || row.get(columnIndex).isEmpty() ) {
            try {
                row.set(columnIndex, generator.nextID());
            } catch ( IDNotFoundException e ) {
                cli.error("Cannot generate ID: %s", e.getMessage());
            }
        }
        output.append(String.join(outputSep, row));
        output.append('\n');
    }

    private IAutoIDGenerator getIDGenerator() {
        IAutoIDGenerator gen = null;
        if ( idGenOpts.prefix != null ) {
            if ( idGenOpts.min == -1 ) {
                cli.error("Missing --min option, required with --prefix");
            }
            if ( idGenOpts.max == -1 ) {
                idGenOpts.max = idGenOpts.min + 1000;
            }
            String format = String.format("%s%%0%dd", idGenOpts.prefix, idGenOpts.width);
            gen = new SequentialIDGenerator(format, idGenOpts.min, idGenOpts.max, (id) -> false);
        } else {
            try {
                IDRange rng = IDPolicyHelper.getRange(idGenOpts.range, new String[] { "dicer" }, idGenOpts.policy);
                gen = new SequentialIDGenerator(rng, (id) -> false);
            } catch ( InvalidIDPolicyException | IDRangeNotFoundException | IOException e ) {
                cli.error("Cannot use ID policy file: %s", e.getMessage());
            }
        }

        return idGenOpts.shortFormat ? new ShortenedIDGenerator(gen) : gen;
    }

}
