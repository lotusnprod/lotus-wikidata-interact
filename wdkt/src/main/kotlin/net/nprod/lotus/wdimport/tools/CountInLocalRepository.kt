/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.tools

import net.nprod.lotus.rdf.RepositoryManager
import net.nprod.lotus.wdimport.wd.InstanceItems

fun countInLocalRepository(
    repositoryManager: RepositoryManager?,
    instanceItems: InstanceItems,
    logger: org.slf4j.Logger,
) {
    repositoryManager?.repository?.let {
        val query =
            """
            SELECT * {
               { SELECT (count (distinct ?org) as ?orgcount) WHERE {
               ?org <${instanceItems.instanceOf.iri}> <${instanceItems.taxon.iri}>.
               }
               }
               
               { SELECT (count (distinct ?cpd) as ?cpdcount) WHERE {
               ?cpd <${instanceItems.instanceOf.iri}> <${instanceItems.typeOfAChemicalEntity.iri}>.
               }
               }
            }
            """.trimIndent()
        logger.info(query)
        val bindingSet =
            it.connection
                .prepareTupleQuery(query)
                .evaluate()
                .first()
        val orgcount = bindingSet.getBinding("orgcount").value.stringValue()
        val cpdcount = bindingSet.getBinding("cpdcount").value.stringValue()

        logger.info("We have $orgcount taxa and $cpdcount compounds in the local repository")
    }
}
