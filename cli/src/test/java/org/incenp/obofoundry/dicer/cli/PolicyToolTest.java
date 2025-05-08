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

public class PolicyToolTest extends CLITestBase {

    @Test
    void testSimpleReadAndWriteBack() throws IOException {
        runCommand(0, "input/myont-idranges.owl", "myont-idranges.owl", null);
    }

    @Test
    void testAddingNewRange() throws IOException {
        runCommand(0, "input/myont-idranges.owl", "new-range.owl", new String[] {
                "--add-range", "user3",
                "--size", "30000"
        });
    }

    protected String getCommand() {
        return "policy";
    }
}
