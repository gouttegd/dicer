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
public class IDPolicy {

    private String name;
    private String prefix;
    private String prefixName;
    private int width;
    private int lastId;
    private int maxBound;
    private Map<String, IDRange> rangesByName;
    private Map<Integer, IDRange> rangesByID;

    /**
     * Creates a new policy for a typical OBO ontology.
     * <p>
     * This assumes the ontology uses the {@code http://purl.obolibrary.org/obo/}
     * prefix, and that IDs should be made of 7 digits.
     * 
     * @param projectId The OBO “project ID” (or “short name”) for the ontology.
     */
    public IDPolicy(String projectId) {
        this(projectId, 7);
    }

    /**
     * Creates a new policy for a typical OBO ontology, with a custom ID width.
     * 
     * @param projectId The OBO “project ID” for the ontology.
     * @param width     The number of digits in IDs.
     */
    public IDPolicy(String projectId, int width) {
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
    public IDPolicy(String name, String prefix, String prefixName, int width) {
        if ( width < 1 || width > 9 ) {
            throw new IllegalArgumentException("Width value out of bounds");
        }
        this.name = name;
        this.prefix = prefix;
        this.prefixName = prefixName;
        this.width = width;
        rangesByName = new HashMap<>();
        rangesByID = new HashMap<>();
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
     * Gets a format string suitable to mind IDs conforming to this policy.
     * 
     * @return The policy’s format string.
     */
    public String getFormat() {
        return prefix + String.format("%%0%dd", width);
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
        ArrayList<IDRange> list = new ArrayList<IDRange>(rangesByID.values());
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
        ArrayList<IDRange> list = new ArrayList<IDRange>(rangesByID.values());
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
                unallocated.add(new IDRange(0, "Unallocated", null, start, range.getLowerBound() - start, this));
            }
            start = range.getUpperBound();
        }
        if ( start < maxBound ) {
            unallocated.add(new IDRange(0, "Unallocated", null, start, maxBound - start, this));
        }

        return unallocated;
    }

    /**
     * Gets the range allocated to a given user.
     * 
     * @param name The name of the user for which to retrieve the range.
     * @return The range allocated to the user, or {@code null} if the policy does
     *         not contain a range for that user.
     * @deprecated Use {@link #findRange(String)} or {@link #getRange(String)}
     *             instead.
     */
    @Deprecated
    public IDRange getRangeFor(String name) {
        return rangesByName.get(name);
    }

    /**
     * Finds the range allocated to a given user.
     * <p>
     * Note that this method, as all similar methods ({@link #findAnyRange(List)},
     * {@link #getRange(String)}, and {@link #getAnyRange(List)}) will return the
     * <em>last registered range</em> for the requested name, in the event that
     * several ranges were registered for the same name.
     * 
     * @param name The name of the user for which to retrieve the range.
     * @return Optional of the requested range, or Optional.empty if the policy does
     *         not contain any range with that name.
     */
    public Optional<IDRange> findRange(String name) {
        return Optional.ofNullable(rangesByName.get(name));
    }

    /**
     * Finds a range allocated to any of the given users.
     * 
     * @param names A list of user names for which to retrieve a range.
     * @return Optional of the first range found, or Optional.empty if the policy
     *         does not contain any range for any of the provided names.
     */
    public Optional<IDRange> findAnyRange(List<String> names) {
        for ( String name : names ) {
            IDRange rng = rangesByName.get(name);
            if ( rng != null ) {
                return Optional.of(rng);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the range allocated to a given user.
     * <p>
     * This is similar to {@link #findRange(String)}, except that it assumes the
     * range exists and consequently throws an exception if it does not.
     * 
     * @param name The name of the user for which to retrieve the range.
     * @return The requested range.
     * @throws IDRangeNotFoundException If there is no range associated to the given
     *                                  name.
     */
    public IDRange getRange(String name) throws IDRangeNotFoundException {
        IDRange rng = rangesByName.get(name);
        if ( rng == null ) {
            throw new IDRangeNotFoundException(name);
        }
        return rng;
    }

    /**
     * Gets a range allocated to any of the given users.
     * <p>
     * This is similar to {@link #findAnyRange(List)}, except that it assumes that
     * at least one of the ranges exists and consequently throws an exception if no
     * range is found.
     * 
     * @param names A list of user names for which to retrieve a range.
     * @return The first range found.
     * @throws IDRangeNotFoundException If there is no range associated to any of
     *                                  the given names.
     */
    public IDRange getAnyRange(List<String> names) throws IDRangeNotFoundException {
        for ( String name : names ) {
            IDRange rng = rangesByName.get(name);
            if ( rng != null ) {
                return rng;
            }
        }
        throw new IDRangeNotFoundException();
    }

    /**
     * Adds a pre-defined range.
     * <p>
     * This is intended for the {@link IDPolicyReader} class, to add a range read
     * from a policy file.
     * 
     * @param id      The ID of the range to add.
     * @param name    The user the range is allocated to.
     * @param comment A comment associated with the range. May be {@code null}.
     * @param lower   The lower bound (inclusive) of the range.
     * @param upper   The upper bound (exclusive) of the range.
     * @throws InvalidIDPolicyException If the range is invalid, if it overlaps with
     *                                  another range in the policy, or the policy
     *                                  already contains a range with the same ID.
     */
    protected void addRange(int id, String name, String comment, int lower, int upper)
            throws InvalidIDPolicyException {
        if ( lower < 0 || lower >= upper || upper > maxBound ) {
            throw new InvalidIDPolicyException("Invalid ID range [%d..%d) for \"%s\"", lower, upper, name);
        }
        if ( rangesByID.containsKey(id) ) {
            throw new InvalidIDPolicyException("Range ID %d already in use", id);
        }
        for ( IDRange r : rangesByID.values() ) {
            if ( !(upper <= r.getLowerBound() || lower >= r.getUpperBound()) ) {
                throw new InvalidIDPolicyException(
                        "Range [%d..%d) for \"%s\" overlaps with range [%d..%d) for \"%s\"", lower, upper, name,
                        r.getLowerBound(), r.getUpperBound(), r.getName());
            }
        }
        IDRange rng = new IDRange(id, name, comment, lower, upper - lower, this);
        rangesByID.put(id, rng);
        rangesByName.put(name, rng);

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
        if ( width < 0 ) {
            throw new IllegalArgumentException("Invalid negative range width");
        }
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
     * @return The newly allocated range.
     * @throws IDRangeNotFoundException If there is no available ID space large
     *                                  enough for the requested range.
     */
    public IDRange addRange(String name, String comment, int size) throws IDRangeNotFoundException {
        int start = findOpenRange(size);
        if ( start == -1 ) {
            throw new IDRangeNotFoundException("Not enough space for a %d-wide range", size);
        }
        IDRange rng = new IDRange(++lastId, name, comment, start, size, this);
        rangesByID.put(rng.getID(), rng);
        rangesByName.put(name, rng);

        return rng;
    }
}
