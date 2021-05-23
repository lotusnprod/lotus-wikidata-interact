/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.formats

import com.univocity.parsers.tsv.TsvWriter
import com.univocity.parsers.tsv.TsvWriterSettings
import net.nprod.lotus.wikidata.download.processors.doWithEachCompound
import net.nprod.lotus.wikidata.download.processors.doWithEachCompoundReferenceTaxon
import net.nprod.lotus.wikidata.download.processors.doWithEachReference
import net.nprod.lotus.wikidata.download.processors.doWithEachTaxon
import org.eclipse.rdf4j.repository.Repository
import java.io.File

fun writeTSVFileWith(file: File, vararg headers: String, f: TsvWriter.() -> Unit) {
    val writer = TsvWriter(file.bufferedWriter(), TsvWriterSettings())
    writer.writeHeaders(headers.toList())
    writer.apply {
        f()
    }
    writer.close()
}

fun compoundReferenceTaxonListToTSV(repository: Repository, file: File) {
    writeTSVFileWith(file, "compound", "reference", "taxon") {
        doWithEachCompoundReferenceTaxon(repository) {
            writeRow(arrayOf(it.compound, it.reference, it.taxon))
        }
    }
}

fun referenceListToTSV(repository: Repository, file: File) {
    writeTSVFileWith(file, "wikidataId", "dois_pipe_separated", "title") {
        doWithEachReference(repository) {
            writeRow(it.wikidataId, it.dois.joinToString("|"), it.title ?: "")
        }
    }
}

fun taxonListToTSV(repository: Repository, file: File) {
    writeTSVFileWith(file, "wikidataId", "names_pipe_separated", "rank") {
        doWithEachTaxon(repository) {
            writeRow(it.wikidataId, it.names.joinToString("|"), it.rank ?: "unspecified")
        }
    }
}

fun compoundsToTSV(repository: Repository, file: File) {
    writeTSVFileWith(file, "wikidataId", "canonicalSmiles", "isomericSmiles", "inchi", "inchiKey") {
        doWithEachCompound(repository) {
            writeRow(
                it.wikidataId,
                it.canonicalSmiles.joinToString("|"),
                it.isomericSmiles.joinToString("|"),
                it.inchis.joinToString("|"),
                it.inchiKeys.joinToString("|")
            )
        }
    }
}
