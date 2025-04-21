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

package org.incenp.obofoundry.dicer.cli;

import java.io.PrintStream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.ParseResult;

/**
 * A command-line utility to manipulate IDs and ID policies.
 */
@Command(name = "dicer-cli",
         description = "Dicer ID library utility",
         optionListHeading = "%nGeneral options:%n",
         commandListHeading = "%nCommands:%n",
         footer = "Report bugs to <dgouttegattat@incenp.org>.",
         footerHeading = "%n",
         mixinStandardHelpOptions = true,
         versionProvider = SimpleCLI.class)
public class SimpleCLI implements IVersionProvider, IExecutionExceptionHandler
{
    public static void main(String[] args) {
        System.exit(run(args));
    }

    /*
     * This is the real entry point. It is separate from the main method above so
     * that it can be called from the test suite without terminating the testing
     * process.
     */
    public static int run(String[] args) {
        SimpleCLI cli = new SimpleCLI();
        int rc = new picocli.CommandLine(cli)
                .setExecutionExceptionHandler(cli)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setUsageHelpLongOptionsMaxWidth(23)
                .setUsageHelpAutoWidth(true)
                .addSubcommand(new PolicyTool())
                .addSubcommand(new HelpCommand())
                .execute(args);
        return rc;
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult fullParseResult)
            throws Exception {
        if ( ex.getMessage() != null ) {
            print(System.err, ex.getMessage());
        } else {
            print(System.err, "Unknown exception: %s", ex.toString());
        }
        return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[] {
                "dicer-cli (Dicer " + SimpleCLI.class.getPackage().getImplementationVersion() + ")",
                "Copyright © 2025 Damien Goutte-Gattat", "",
                "This program is released under the GNU General Public License.",
                "See the COPYING file or <http://www.gnu.org/licenses/gpl.html>." };
    }

    /**
     * Prints an informative message on standard output.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void info(String format, Object... args) {
        print(System.out, format, args);
    }

    /**
     * Prints a warning message on standard error output.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void warn(String format, Object... args) {
        print(System.err, format, args);
    }

    /**
     * Prints an error message on standard error output and exits the application.
     * 
     * @param format The message to print, as a format string.
     * @param args   Arguments for the format specifiers inside the message.
     */
    public void error(String format, Object... args) {
        // Throw an exception to interrupt the application; the exception will be caught
        // by PicoCLI and the error message will be displayed by the exception handler.
        throw new RuntimeException(String.format(format, args));
    }

    /*
     * Common code for the info/warn/error methods.
     */
    private void print(PrintStream stream, String format, Object... args) {
        stream.append("dicer-cli: ");
        stream.printf(format, args);
        stream.append('\n');
    }
}
