/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.download.modes.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.nprod.lotus.wikidata.download.formats.compoundReferenceTaxonListToTSV
import net.nprod.lotus.wikidata.download.formats.compoundsToTSV
import net.nprod.lotus.wikidata.download.formats.referenceListToTSV
import net.nprod.lotus.wikidata.download.formats.taxonListToTSV
import net.nprod.lotus.wikidata.download.rdf.RDFRepository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.slf4j.LoggerFactory
import java.io.File

fun export(repositoryLocation: File, outputDirectory: File, direct: Boolean) = GlobalScope.launch {
    val logger = LoggerFactory.getLogger("export")
    val rdfRepository = RDFRepository(repositoryLocation)

    val repository = if (direct) {
        SPARQLRepository("https://query.wikidata.org/sparql")
    } else {
        rdfRepository.repository
    }

    logger.info("Exporting from the repository: $repositoryLocation")

    launch(Dispatchers.IO) {
        logger.info("Preparing to run the compounds")
        compoundsToTSV(repository, File(outputDirectory, "compounds.tsv"))
        logger.info("Finished the compounds")
    }

    launch(Dispatchers.IO) {
        logger.info("Preparing to run the references")
        referenceListToTSV(repository, File(outputDirectory, "references.tsv"))
        logger.info("Finished the references")
    }

    launch(Dispatchers.IO) {
        logger.info("Preparing to run the taxa")
        taxonListToTSV(repository, File(outputDirectory, "taxa.tsv"))
        logger.info("Finished the taxa")
    }

    launch(Dispatchers.IO) {
        logger.info("Preparing to run the compoundReferenceTaxon")
        compoundReferenceTaxonListToTSV(repository, File(outputDirectory, "compound_reference_taxon.tsv"))
        logger.info("Finished the CRT")
    }
}
