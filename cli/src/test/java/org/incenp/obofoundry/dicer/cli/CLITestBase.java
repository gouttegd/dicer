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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

public abstract class CLITestBase {

    /**
     * Runs the command with the specified arguments.
     * 
     * @param code      The expected return code.
     * @param arguments The arguments of the command to run.
     */
    protected void runCommand(int code, List<String> arguments) {
        ArrayList<String> tmpArgs = new ArrayList<>();
        tmpArgs.add(getCommand());
        if ( arguments != null ) {
            tmpArgs.addAll(arguments);
        }
        String[] args = new String[tmpArgs.size()];
        tmpArgs.toArray(args);

        Assertions.assertEquals(code, SimpleCLI.run(args));
    }

    /**
     * Runs the command with an input file to read and an output file to write.
     * 
     * @param code   The expected return code.
     * @param input  The input file the command should read (may be {@code null}).
     * @param output The output file the command is expected to write; if not
     *               {@code null}, the file actually written by the command will be
     *               compared against that file.
     * @param others Other arguments to pass to the command.
     * @throws IOException If any I/O error occurs when writing or comparing the
     *                     files.
     */
    protected void runCommand(int code, String input, String output, String[] others) throws IOException {
        ArrayList<String> args = new ArrayList<>();

        if ( input != null ) {
            args.add(getInputPath(input));
        }

        if ( output != null ) {
            args.add("--output");
            args.add(getOutputPath(output));
        }

        if ( others != null ) {
            for ( String arg : others ) {
                args.add(arg);
            }
        }

        runCommand(code, args);

        if ( code == 0 && output != null ) {
            checkOutput(output);
        }
    }

    /**
     * Checks that an output file has been written exactly as expected.
     * 
     * @param output The base name of the output file.
     * @throws IOException If an I/O error occurs when comparing the files.
     */
    protected void checkOutput(String output) throws IOException {
        String path = getOutputPath(output);
        File expected = new File(path.substring(0, path.length() - 4));
        File written = new File(path);
        boolean same = FileUtils.contentEquals(expected, written);
        Assertions.assertTrue(same);
        if ( same ) {
            written.delete();
        }
    }

    /**
     * Gets the path to an input file.
     * 
     * @param name The base name of the file to find.
     * @return The path to the input file, relatively to the current working
     *         directory when running the tests, if the file was found; otherwise,
     *         the original base name.
     */
    protected String getInputPath(String name) {
        File f = new File("../lib/src/test/resources/" + name);
        if ( !f.exists() ) {
            f = new File("src/test/resources/" + getCommand() + "/" + name);
        }
        if ( !f.exists() ) {
            f = new File("src/test/resources/" + name);
        }
        return f.exists() ? f.getPath() : name;
    }

    /**
     * Gets the path to an output file.
     * 
     * @param name The base name of the output file.
     * @return The path to the output file, as it should be used by the command to
     *         test.
     */
    protected String getOutputPath(String name) {
        return "src/test/resources/" + getCommand() + "/output/" + name + ".out";
    }

    /**
     * Gets the name of the command being test.
     * 
     * @return The command name.
     */
    protected abstract String getCommand();
}
