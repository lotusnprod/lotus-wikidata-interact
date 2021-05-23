# wikidata Lotus Exporter

This project is used to take the LOTUS (and beyond) data from Wikidata and export it in a format
that can be used by our website.

It is made in Kotlin. All you need really is a JVM, and it should gather everything by itself.

All the commands are given from the root of the project, not this directory!

## Build

````
./gradlew :downloadLotus:build
````

## Usage without installing

### Gather WikiData entries related to LOTUS locally

This can take a variable amount of time depending on how loaded the WikiData servers are. On good days it is < 10 min

````
./gradlew -q :downloadLotus:run --args "mirror"
````

### Export entries to TSV

This will export in tsv files in data/output. We have 4 files: references.tsv, compounds.tsv, taxa.tsv
and a file that contains the triples linking these: /compound_reference_taxon.tsv/

````
./gradlew -q :downloadLotus:run --args "export -o data/output"
````


### Running a query on your new local base

This will output a TSV file to the standard output (so your console likely)

````
./gradlew -q :downloadLotus:run --args "query $PWD/queries/getAllCompoundsInChIKeys.sparql"
````

### Running a query directly on WikiData

There is also a way to do the query directly on WikiData

````
./gradlew -q :downloadLotus:run --args "query -d $PWD/queries/getAllCompoundsInChIKeys.sparql"
````


## Installing and deploying on another machine

````
./gradlew :downloadLotus:assembleDist
````

This will produce several files in *build/distributions*. Uncompress the one you want somewhere and you can just run it on any machine that has a JDK like that:

````
./bin/wikidataLotusExporter mirror
````

````
./bin/wikidataLotusExporter query queries/getAllCompoundsInChIKeys.sparql
````

## Format of SPARQL queries

For convenience, there is a little helper for SPARQL queries. If you add at the beginning of your query

````
#!WDDEFAULTIMPORTS
````

It will add the default wikidata prefixes.