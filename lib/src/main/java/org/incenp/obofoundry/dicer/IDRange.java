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
 * Represents an ID range in an ID policy.
 */
public class IDRange {

    private int id;
    private String name;
    private String comment;
    private int lowerBound;
    private int upperBound;
    private IDPolicy policy;

    /**
     * Creates a range with an optional comment.
     * 
     * @param id      The range’s own ID.
     * @param name    The name of the user this range is allocated to.
     * @param comment A comment associated with the range. May be {@code null}.
     * @param start   The lower bound (inclusive) of the range.
     * @param size    The number of IDs in the range.
     * @param policy  The policy this range belongs to.
     */
    protected IDRange(int id, String name, String comment, int start, int size, IDPolicy policy) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.policy = policy;
        lowerBound = start;
        upperBound = start + size;
    }

    /**
     * Gets the numerical identifier for this range.
     * 
     * @return The range’s own ID.
     */
    public int getID() {
        return id;
    }

    /**
     * Gets the name of the user this range is allocated to.
     * 
     * @return The range’s user name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the comment associated with the range, if any.
     * 
     * @return The range’s comment, or {@code null} if the range does not have any
     *         comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the lower bound (inclusive) of the range.
     * 
     * @return The range’s lower bound.
     */
    public int getLowerBound() {
        return lowerBound;
    }

    /**
     * Gets the upper bound (exclusive) of the range.
     * 
     * @return The range’s upper bound.
     */
    public int getUpperBound() {
        return upperBound;
    }

    /**
     * Gets the number of IDs available in this range.
     * 
     * @return The range size.
     */
    public int getSize() {
        return upperBound - lowerBound;
    }

    /**
     * Gets the policy this range belongs to.
     * 
     * @return The ID policy this range belongs to.
     */
    public IDPolicy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return String.format("id=%d, name=%s, bounds=[%d..%d)", id, name, lowerBound, upperBound);
    }
}
