# Lotus_bot

This is a Bot for [WikiData](https://www.wikidata.org). It is used to import the data for the LOTUS and
the [WikiData Natural Products Chemistry project](https://www.wikidata.org/wiki/Wikidata:WikiProject_Chemistry/Natural_products)

It is written in [Kotlin](https://www.kotlinlang.org) and uses:

- [Wikidata Toolkit](https://www.mediawiki.org/wiki/Wikidata_Toolkit)
- [Chemistry Development Kit](https://cdk.github.io/)
- [RDF4J](https://rdf4j.org/)
- the super fast [Univocity Parsers](https://github.com/uniVocity/univocity-parsers) for TSV files
- [Kotlinx.cli](https://github.com/Kotlin/kotlinx-cli)

Requires Java >=11

## Build

## Run

### Import lotus Data

Replace <<foo>> and <<bar>> by your users and password. Notice the space in front of that command
to not store them in your shell history.

This will run a single entry from the file in test mode:

```console
 export WIKIDATA_USER=<<foo>>
 export WIKIDATA_PASSWORD=<<bar>>
./gradlew :uploadLotus:run --args="../data/validated_referenced_structure_organism_pairs.tsv"
```

This will run the full file in test mode:

```console
 export WIKIDATA_USER=<<foo>>
 export WIKIDATA_PASSWORD=<<bar>>
./gradlew :uploadLotus:run --args="../data/validated_referenced_structure_organism_pairs.tsv -l -1"
```

This will run the first line of the file in real mode (you will likely need bot rights to do that:

```console
 export WIKIDATA_USER=<<foo>>
 export WIKIDATA_PASSWORD=<<bar>>
./gradlew :uploadLotus:run --args="../data/validated_referenced_structure_organism_pairs.tsv -r"
```

If you need to produce a jar file (to use on a different machine for example)

```console
./gradlew build
```

The distribution will be available in **build/distributions** (.zip and .tar.gz)

There is work to do to reduce the size of the distribution (use java modules, use only the parts of cdk and rdf4j
we are really using).

#### Allow the creation of new species

This is only for LOTUS developers, please use with caution as it is not perfect yet

```console
export CREATE_SPECIES=yes_and_I_understand_that_I_will_not_complain_if_things_are_wrong
```

## Developers

### If you need to upgrade wiki-java

You need to make sure you did a recursive clone of this repository with

```console
git clone --recursive
```

You can then update the repo and update the jars:

```console
git submodule update
cd external/wiki-java
mvn package  # You need to have maven installed obviously, and this will take some time (2 min on a Ryzen 3700X)
rm ../../libs/wiki-java-*.jar
cp target/wiki-java-*.jar ../../libs
```

Don't forget to update **build.gradle.kts** if the version changed.
