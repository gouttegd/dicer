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
                .addSubcommand(new TSVTool())
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
                "This program is released under the terms of the 3-clause",
                "BSD license. See the COPYING file in the source distribution." };
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
