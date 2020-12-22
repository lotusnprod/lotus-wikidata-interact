// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

fun buildCompoundCache(
    dataTotal: DataTotal,
    repositoryManager: RepositoryManager?,
    instanceItems: InstanceItems,
    logger: Logger,
    wdSparql: ISparql,
    wikidataCompoundCache: MutableMap<InChIKey, String>
) {
    val inchiKeys = dataTotal.compoundCache.store.map { (_, compound) ->
        compound.inchikey
    }
    inchiKeys.chunked(1000) { inchiKeysBlock ->
        repositoryManager?.let {
            val query = """
               SELECT ?id ?inchikey WHERE {
                   ?id <${instanceItems.inChIKey.iri}> ?inchikey.
                   VALUES ?inchikey {
                      ${inchiKeysBlock.joinToString(" ") { key -> Rdf.literalOf(key).queryString }}
                   }
                }
            """.trimIndent()
            logger.info(query)
            wdSparql.query(query) { result ->
                result.forEach {
                    wikidataCompoundCache[it.getValue("inchikey").stringValue()] =
                        it.getValue("id").stringValue().split("/").last()
                }
            }
        }
    }
}