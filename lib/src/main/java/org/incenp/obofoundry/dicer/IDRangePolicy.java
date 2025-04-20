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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a comprehensive ID range policy, that both describes the expected
 * format of ID and allocates ranges to users.
 */
public class IDRangePolicy {

    private String name;
    private String prefix;
    private String prefixName;
    private int width;
    private int lastId;
    private int maxBound;
    private Map<String, IDRange> ranges;

    /**
     * Creates a new policy for a typical OBO ontology.
     * <p>
     * This assumes the ontology uses the {@code http://purl.obolibrary.org/obo/}
     * prefix, and that IDs should be made of 7 digits.
     * 
     * @param projectId The OBO “project ID” (or “short name”) for the ontology.
     */
    public IDRangePolicy(String projectId) {
        this(projectId, 7);
    }

    /**
     * Creates a new policy for a typical OBO ontology, with a custom ID width.
     * 
     * @param projectId The OBO “project ID” for the ontology.
     * @param width     The number of digits in IDs.
     */
    public IDRangePolicy(String projectId, int width) {
        this("http://purl.obolibrary.org/obo/" + projectId,
                "http://purl.obolibrary.org/obo/" + projectId.toUpperCase() + "_", projectId.toUpperCase(), width);
    }

    /**
     * Creates a new custom policy.
     * 
     * @param name       An IRI representing the ontology this policy is intended
     *                   for.
     * @param prefix     The IRI prefix for the IDs to generate.
     * @param prefixName The prefix name for the IDs to generate.
     * @param width      The number of digits in IDs.
     */
    public IDRangePolicy(String name, String prefix, String prefixName, int width) {
        if ( width < 1 || width > 9 ) {
            throw new IllegalArgumentException("Width value out of bounds");
        }
        this.name = name;
        this.prefix = prefix;
        this.prefixName = prefixName;
        this.width = width;
        ranges = new HashMap<String, IDRange>();
        lastId = 0;

        maxBound = 1;
        for ( int i = 0; i < width; i++ ) {
            maxBound *= 10;
        }
    }

    /**
     * Gets the name of the ontology this policy is intended for.
     * <p>
     * For OBO ontologies, this would typically be something like
     * {@code http://purl.obolibrary.org/obo/myont}, where {@code myont} is the
     * ontology’s short name.
     * 
     * @return The name of the ontology.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the prefix IRI for IDs to be created with this policy.
     * <p>
     * For OBO ontologies, this would typically be something like
     * {@code http://purl.obolibrary.org/obo/MYONT_}, where {@code MYONT} is the
     * uppercase form of the ontology’s short name.
     * 
     * @return The IRI prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the prefix name for IDs to be created with this policy.
     * <p>
     * For OBO ontologies, this would typically be the short name of the ontology,
     * converted to uppercase.
     * 
     * @return The prefix name.
     */
    public String getPrefixName() {
        return prefixName;
    }

    /**
     * Gets the number of digits that IDs to be created with this policy should
     * have.
     * 
     * @return The size of the numerical part of IDs.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the highest possible upper bound for IDs in this policy.
     * 
     * @return The upper bound of the highest possible range.
     */
    public int getMaxUpperBound() {
        return maxBound;
    }

    /**
     * Gets all the ID ranges defined in this policy, sorted by their IDs.
     * 
     * @return A list of all the ranges in this policy.
     */
    public List<IDRange> getRangesByID() {
        ArrayList<IDRange> list = new ArrayList<IDRange>(ranges.values());
        list.sort((a, b) -> Integer.compare(a.getID(), b.getID()));
        return list;
    }

    /**
     * Gets all the ID ranges defined in this policy, sorted by the lower bound of
     * each range.
     * 
     * @return A list of all the ranges in this policy.
     */
    public List<IDRange> getRangesByLowerBound() {
        ArrayList<IDRange> list = new ArrayList<IDRange>(ranges.values());
        list.sort((a, b) -> Integer.compare(a.getLowerBound(), b.getLowerBound()));
        return list;
    }

    /**
     * Gets all the non-allocated ranges in this policy, sorted by the lower bound
     * of each range.
     * 
     * @return A list of the free ranges in this policy.
     */
    public List<IDRange> getUnallocatedRanges() {
        ArrayList<IDRange> unallocated = new ArrayList<IDRange>();
        int start = 0;
        for ( IDRange range : getRangesByLowerBound() ) {
            if ( range.getLowerBound() > start ) {
                unallocated.add(new IDRange(0, "Unallocated", null, start, range.getLowerBound() - start));
            }
            start = range.getUpperBound();
        }
        if ( start < maxBound ) {
            unallocated.add(new IDRange(0, "Unallocated", null, start, maxBound - start));
        }

        return unallocated;
    }

    /**
     * Gets the range allocated to a given user.
     * 
     * @param name The name of the user for which to retrieve the range.
     * @return The range allocated to the user, or {@code null} if the policy does
     *         not contain a range for that user.
     * @deprecated Use {@link #getRange(String)} instead.
     */
    @Deprecated
    public IDRange getRangeFor(String name) {
        return ranges.get(name);
    }

    /**
     * Gets the range allocated to a given user.
     * 
     * @param name The name of the user for which to retrieve the range.
     * @return Optional of the requested range, or Optional.empty if the policy does
     *         not contain any range with that name.
     */
    public Optional<IDRange> getRange(String name) {
        return Optional.ofNullable(ranges.get(name));
    }

    /**
     * Adds a pre-defined range.
     * <p>
     * This is intended for the {@link IDRangePolicyReader} class, to add a range
     * read from a policy file.
     * 
     * @param id      The ID of the range to add.
     * @param name    The user the range is allocated to.
     * @param comment A comment associated with the range. May be {@code null}.
     * @param lower   The lower bound (inclusive) of the range.
     * @param upper   The upper bound (exclusive) of the range.
     * @throws InvalidIDRangePolicyException If the range is invalid, or if it
     *                                       overlaps with another range in the
     *                                       policy.
     */
    protected void addRange(int id, String name, String comment, int lower, int upper)
            throws InvalidIDRangePolicyException {
        if ( lower < 0 || lower >= upper || upper > maxBound ) {
            throw new InvalidIDRangePolicyException("Invalid ID range: [%d..%d)", lower, upper);
        }
        for ( IDRange r : ranges.values() ) {
            if ( !(upper <= r.getLowerBound() || lower >= r.getUpperBound()) ) {
                throw new InvalidIDRangePolicyException(
                        "Range [%d..%d) for \"%s\" overlaps with range [%d..%d) for \"%s\"", lower, upper, name,
                        r.getLowerBound(), r.getUpperBound(), r.getName());
            }
        }
        IDRange rng = new IDRange(id, name, comment, lower, upper - lower);
        ranges.put(name, rng);

        if ( id > lastId ) {
            lastId = id;
        }
    }

    /**
     * Finds an available range with the given size in the policy.
     * 
     * @param width The size of the range to find.
     * @return The lower bound of the lowest available range in the policy with the
     *         desired size, or -1 if no available range was found.
     */
    public int findOpenRange(int width) {
        int start = 0;
        boolean found = false;
        for ( IDRange rng : getRangesByLowerBound() ) {
            int end = start + width;
            if ( end <= rng.getLowerBound() && end <= maxBound ) {
                found = true;
                break;
            }
            start = rng.getUpperBound();
        }

        if ( start + width <= maxBound ) {
            found = true;
        }

        return found ? start : -1;
    }

    /**
     * Adds a new range to this policy.
     * 
     * @param name    The name of the user the range is allocated to.
     * @param comment A comment associated with the range (may be {@code null}.
     * @param size    The size of the range to allocate.
     * @return The newly allocated range, or {@code null} if no available range of
     *         the desired size was available.
     */
    public IDRange addRange(String name, String comment, int size) {
        IDRange rng = null;

        int start = findOpenRange(size);
        if ( start != -1 ) {
            rng = new IDRange(++lastId, name, comment, start, size);
            ranges.put(name, rng);
        }

        return rng;
    }
}
