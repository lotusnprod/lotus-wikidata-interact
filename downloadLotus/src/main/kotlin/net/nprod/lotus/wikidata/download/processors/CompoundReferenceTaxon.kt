/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */
package net.nprod.lotus.wikidata.download.processors

import net.nprod.lotus.wikidata.download.models.CompoundReferenceTaxon
import net.nprod.lotus.wikidata.download.sparql.LOTUSQueries
import org.eclipse.rdf4j.common.transaction.IsolationLevels
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection

val queryForCompoundsRefsAndTaxa =
    """
    ${LOTUSQueries.prefixes}
    SELECT DISTINCT ?compound_id ?taxon_id ?reference_id
    WHERE {
      ?compound_id     p:P703 ?pp703.
      ?pp703           ps:P703 ?taxon_id;
                       prov:wasDerivedFrom/pr:P248 ?reference_id.
    }
    """.trimIndent()

/**
 * Iterates over each CompoundReferenceTaxon found in the repository and applies the given function [f].
 *
 * @param repository The RDF4J repository to query.
 * @param f Function to apply to each CompoundReferenceTaxon.
 *
 * This function does not write to the repository and uses no isolation.
 */
fun doWithEachCompoundReferenceTaxon(
    repository: Repository,
    f: (CompoundReferenceTaxon) -> Unit,
) {
    repository.connection.use { conn: RepositoryConnection ->
        conn.begin(IsolationLevels.NONE) // We are not writing anything

        conn.prepareTupleQuery(queryForCompoundsRefsAndTaxa).evaluate().forEach {
            // Extract values from the query result
            val compoundId = it.getValue("compound_id").stringValue()
            val taxonId = it.getValue("taxon_id").stringValue()
            val referenceId = it.getValue("reference_id").stringValue()
            // Apply the provided function
            f(CompoundReferenceTaxon(compoundId, taxonId, referenceId))
        }
        conn.commit()
    }
}
