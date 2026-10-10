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
     * Gets a format string suitable to mind IDs conforming to the policy this range
     * belongs to.
     * <p>
     * This is a convenience method so that client code can mint IDs with just a
     * IDRange object, without needing a reference to the policy.
     * 
     * @return The policy’s format string.
     */
    public String getFormat() {
        return policy.getFormat();
    }

    @Override
    public String toString() {
        return String.format("id=%d, name=%s, bounds=[%d..%d)", id, name, lowerBound, upperBound);
    }
}
