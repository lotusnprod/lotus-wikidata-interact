/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.processors

import net.nprod.lotus.wikidata.download.models.Reference
import net.nprod.lotus.wikidata.download.rdf.vocabulary.Wikidata
import net.nprod.lotus.wikidata.download.rdf.vocabulary.WikidataBibliography
import org.eclipse.rdf4j.IsolationLevels
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection

fun doWithEachReference(
    repository: Repository,
    f: (Reference) -> Unit
) {
    repository.connection.use { conn: RepositoryConnection ->
        conn.begin(IsolationLevels.NONE) // We are not writing anything
        conn.prepareTupleQuery(
            """
            SELECT ?article_id ?doi ?title {
                ?article_id <${Wikidata.Properties.instanceOf}> ?type;
                            <${WikidataBibliography.Properties.doi}> ?doi.
                OPTIONAL { ?article_id <${WikidataBibliography.Properties.title}> ?title. }
            }
            """.trimIndent()
        ).evaluate().groupBy { it.getValue("article_id").stringValue() }.forEach { (key, value) ->
            f(
                Reference(
                    wikidataId = key,
                    dois = value.mapNotNull { it.getValue("doi")?.stringValue() },
                    title = value.mapNotNull { it.getValue("title")?.stringValue() }.firstOrNull()
                )
            )
        }
        conn.commit()
    }
}
