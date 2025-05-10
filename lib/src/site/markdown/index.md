The Dicer library
=================

The _Dicer_ Java library (groupId: `org.incenp`, artifactId:
`dicer-lib`) provides several classes to work with OBO Foundry-style
_identifiers policy_.

Using a ID policy file
----------------------
Use the [IDPolicyReader](../apidocs/org/incenp/obofoundry/dicer/IDPolicyReader.html)
to parse a ID policy file:

```java
IDPolicyReader reader = new IDPolicyReader();
IDPolicy policy = reader.read("myont-idranges.owl");
```

The returned [IDPolicy](../apidocs/org/incenp/obofoundry/dicer/IDPolicy.html)
object then allows to

* get details about the policy, such as the format of identifiers;
* get a list of all ID ranges in the policy, or unallocated ranges;
* find a range allocated to a given user;
* allocate a new range in the unallocated ID space.

When the policy object has been modified (by allocating new ranges), use
the [IDPolicyWriter](../apidocs/org/incenp/obofoundry/dicer/IDPolicyWriter.html)
class to write it back to a file.

The
[IDPolicyHelper](../apidocs/org/incenp/obofoundry/dicer/IDPolicyHelper.html)
class provides static methods to facilitate working with a ID policy.
Notably, if all that is needed is to get the ID range allocated to a
given user, the `getRange()` method provides an easy way to do that:

```java
IDRange range = getRange("Alice", null, null);
```

This will automatically get the range allocated to the user _Alice_ in
the default policy file (any file in the current directory with a name
ending with `-idranges.owl`).

Minting new identifiers
-----------------------

The[IAutoIDGenerator](../apidocs/org/incenp/obofoundry/dicer/IAutoIDGenetrator.html)
interface represents any object that can generate new identifiers. The
library provides two different implementations of it:

* [SequentialIDGenerator](../apidocs/org/incenp/obofoundry/dicer/SequentialIDGenerator.html),
  to generate IDs with numerical suffixes picked sequentially within a
  range;
* [RandomizedIDGenerator](../apidocs/org/incenp/obofoundry/dicer/RandomizedIDGenerator.html),
  to generate IDs with numerical suffixes picked randomly within a
  range.

Once a generator has been identified, simply call the `nextID()` method
to mint a new identifier.

The generators can be identified with an implementation of
[IExistenceChecker](../apidocs/org/incenp/obofoundry/dicer/IExistenceChecker.html)
to ensure they only generate IDs that are not already in use.
