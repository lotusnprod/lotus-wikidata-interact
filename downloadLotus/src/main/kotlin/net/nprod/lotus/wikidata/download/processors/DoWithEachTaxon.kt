/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.processors

import net.nprod.lotus.wikidata.download.models.Taxon
import net.nprod.lotus.wikidata.download.rdf.vocabulary.WikidataChemistry
import net.nprod.lotus.wikidata.download.rdf.vocabulary.WikidataTaxonomy
import net.nprod.lotus.wikidata.download.sparql.LOTUSQueries
import org.eclipse.rdf4j.common.transaction.IsolationLevels
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection

fun doWithEachTaxon(
    repository: Repository,
    f: (Taxon) -> Unit,
) {
    repository.connection.use { conn: RepositoryConnection ->
        conn.begin(IsolationLevels.NONE) // We are not writing anything
        val query =
            """
            ${LOTUSQueries.prefixes}
            SELECT DISTINCT ?taxon_id ?taxon_name ?taxon_rank {
              ?something <${WikidataChemistry.Properties.foundInTaxon}> ?taxon_id.
              OPTIONAL { ?taxon_id <${WikidataTaxonomy.Properties.taxonName}> ?taxon_name. } # because of Q1865281 
              OPTIONAL { ?taxon_id <${WikidataTaxonomy.Properties.taxonRank}>/rdfs:label ?taxon_rank.
                         FILTER (lang(?taxon_rank) = 'en')
              }
              
              #OPTIONAL { ?taxon_id ${WikidataTaxonomy.Properties.parentTaxonChain} ?parent_id. }
              
            }
            """.trimIndent()
        conn
            .prepareTupleQuery(query)
            .evaluate()
            .groupBy { it.getValue("taxon_id").stringValue() }
            .forEach { (key, value) ->
                f(
                    Taxon(
                        wikidataId = key,
                        names = value.mapNotNull { it.getValue("taxon_name")?.stringValue() }.distinct(),
                        rank = value.mapNotNull { it.getValue("taxon_rank")?.stringValue() }.firstOrNull(),
                    ),
                )
            }
        conn.commit()
    }
}
