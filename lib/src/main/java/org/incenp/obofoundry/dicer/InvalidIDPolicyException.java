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

package org.incenp.obofoundry.dicer;

/**
 * An exception that is thrown when a ID policy is invalid.
 */
public class InvalidIDPolicyException extends IDException {

    private static final long serialVersionUID = 2401919726320928281L;

    /**
     * Creates a new instance with the specified error message.
     * 
     * @param msg  A message describing the error.
     * @param args Arguments to substitute in the {@code msg} format string.
     */
    public InvalidIDPolicyException(String msg, Object... args) {
        super(msg, args);
    }
}
