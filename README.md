# Wikidata interactions for the LOTUS Initiative

> [!IMPORTANT]
> **This repository has moved.** Development continues at **[github.com/lotus-initiative](https://github.com/lotus-initiative)**.
> This repo is archived and kept only for backward compatibility. Please head over there for the latest code, issues, and releases.

This project is divided in multiple parts:

- [uploadLotus](uploadLotus/README.md) : Used to upload LOTUS data to Wikidata
- [downloadLotus](downloadLotus/README.md) : Used to download LOTUS data from Wikidata
  It is also used by uploadLotus to check what already exists on Wikidata
- [importPublication](importPublication/README.md) : Helper to create an entry corresponding to a scientific article on
  Wikidata. Takes a DOI as input and creates the page with all authors, journal, etc.
- [WDKt](wdkt/) : A library to interact with Wikidata in Kotlin (used by the uploader), this may get out of this repository.
