The tsv command
===============

The `dicer-cli tsv` subcommand allows to fill a column within a TSV file
with newly generated IDs.

Example
-------
The following command:

```sh
$ dicer-cli tsv input.tsv \
            --prefix http://purl.obolibrary.org/obo/MYONT_ \
            --min-id 1000 \
            --column 2 \
            --output output.tsv
```

will fill the second column of the `input.tsv` file with IDs of the form
`http://purl.obolibrary.org/obo/MYONT_000ZZZZ`, with _ZZZZ_ starting at
1,000, and write the result into `output.tsv`.

Overwriting
-----------
By default, the command will overwrite any existing ID already found in
the ID column of the input file.

To prevent overwriting of _any_ pre-existing ID, use the
`--no-overwrite` option.

Alternatively, use the `--overwrite-prefix PFX` option to overwrite only
the pre-existing IDs that start with the specified `PFX` prefix.
