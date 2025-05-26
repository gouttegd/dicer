The policy command
==================

Usages
------

The `dicer-cli policy` subcommand allows performing the following tasks:

### Validating a policy file
Simply call the command with the name of the policy file to validate:

```sh
$ dicer-cli policy myont-idranges.owl
```

The command will exit with a return code of zero if the file contains a
valid policy; otherwise, an error message will be printed and the
command will exit with a non-zero value.

### Re-serialising a policy file
Add the `--save` option to force the command to write the policy back to
the original input file:

```sh
$ dicer-cli policy myont-idranges.owl --save
```

To re-serialise the policy into a different file, use the `--output`
option instead:

```sh
$ dicer-cli policy myont-idranges.owl --output reserialised-idranges.owl
```

### Listing ranges in the policy
Use the `--list` option to print the ranges defined in the policy,
sorted by their bounds (lower ranges first):

```sh
$ dicer-cli policy myont-idranges.owl --list
Alice: [0..10000)
Bob: [50000..60000)
```

### Allocating a new range
To automatically allocate a new range of 20,000 IDs to the user
_Charlie_:

```sh
$ dicer-cli policy myont-idranges --add-range Charlie --size 20000
dicer-cli: Allocated range [10000..30000) for user "Charlie"
```

Allocating a new range automatically implies the `--save` option, so
that the modified policy with its new range is automatically saved to
its original file. To save it to a different file, add the `--output`
option.

Common options
--------------

Use the `--show-owlapi-error` option to make the command print the error
message from the OWLAPI in case of a syntactically incorrect file. The
default behaviour is _not_ to show that message, because it is very
verbose and of very little use for most users.

By default, the command will accept an ID policy file written in any
OWL syntax supported by the OWLAPI. By convention though, OBO ID policy
files are written in OWL Manchester syntax, and the `policy` command can
be instructed to specifically expect a file in that syntax using the
`--assume-manchester` option. This is especially useful when combined
with the `--show-owlapi-error` option above, because then in case of a
syntactically incorrect file, the command will only report the error
from the Manchester parser, instead of all the errors from all the
parsers available in the OWLAPI.
