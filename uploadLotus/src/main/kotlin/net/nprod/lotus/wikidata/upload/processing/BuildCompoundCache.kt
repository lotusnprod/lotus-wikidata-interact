/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.processing

import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.rdf.RepositoryManager
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

/**
 * Amount of compounds to query by SPARQL to get the compound info from the InChiKeys
 */
const val COMPOUNDS_PROCESSING_CHUNK_SIZE: Int = 1000

/**
 * Used to build a local cache of the compounds so we are querying only at the beginning of the run and not for each
 * new entry.
 */
fun DataTotal.buildCompoundCache(
    repositoryManager: RepositoryManager?,
    instanceItems: InstanceItems,
    logger: org.slf4j.Logger,
    wdSparql: ISparql,
    wikidataCompoundCache: MutableMap<InChIKey, String>
) {
    val inchiKeys = this.compoundCache.store.map { (_, compound) ->
        compound.inchikey
    }
    inchiKeys.chunked(COMPOUNDS_PROCESSING_CHUNK_SIZE) { inchiKeysBlock ->
        repositoryManager?.let {
            val query =
                """
               SELECT ?id ?inchikey WHERE {
                   ?id <${instanceItems.inChIKey.iri}> ?inchikey.
                   VALUES ?inchikey {
                      ${inchiKeysBlock.joinToString(" ") { key -> Rdf.literalOf(key).queryString }}
                   }
                }
                """.trimIndent()
            logger.info(query)
            wdSparql.selectQuery(query) { result ->
                result.forEach {
                    wikidataCompoundCache[it.getValue("inchikey").stringValue()] =
                        it.getValue("id").stringValue().split("/").last()
                }
            }
        }
    }
}
