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

package org.incenp.obofoundry.dicer;

/**
 * An exception that is thrown when a requested range cannot be found in a ID
 * range policy.
 */
public class IDRangeNotFoundException extends IDException {

    private static final long serialVersionUID = -4525501693310828709L;

    /**
     * Creates a new instance with the specified error message.
     * 
     * @param msg  A message describing the error.
     * @param args Arguments to substitute in the {@code msg} format string.
     */
    public IDRangeNotFoundException(String msg, Object... args) {
        super(msg, args);
    }

    /**
     * Creates a new instance with the name of the unfound range.
     * 
     * @param name The name of the range that could not be found in the policy.
     */
    public IDRangeNotFoundException(String name) {
        super("No range '%s' found in ID policy", name);
    }

    /**
     * Creates a new instance with a generic error message.
     */
    public IDRangeNotFoundException() {
        super("No suitable range found in ID policy");
    }

}
