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

import java.util.List;

/**
 * An interface to react to events from a {@link TSVReader} object.
 */
public interface ITSVListener {
    /**
     * Called upon reading a comment (a line starting with a {@code #} character.
     * 
     * @param comment The comment that has been read. This does not include the
     *                initial {@code #} character nor the terminating newline.
     */
    public void onComment(String comment);

    /**
     * Called upon reading the first non-comment line.
     * 
     * @param header    The values from the first non-comment line.
     * @param separator The separator character.
     */
    public void onHeader(List<String> header, char separator);

    /**
     * Called upon reading a data row.
     * 
     * @param row The values from the row that has just been read.
     */
    public void onRow(List<String> row);
}
