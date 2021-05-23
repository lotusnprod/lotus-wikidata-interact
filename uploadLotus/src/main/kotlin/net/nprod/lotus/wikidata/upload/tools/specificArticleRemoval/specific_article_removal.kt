/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.tools.specificArticleRemoval

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak

/**
 * This script is used to clean up entries we added that are listing the wrong article
 * because it matches an article with a DOI of NA.
 **/

data class Statement(
    val compoundId: String,
    val taxonId: String,
    val referenceId: String
)

fun main() {
    val instanceItems = MainInstanceItems
    val publisher = WDPublisher(instanceItems, pause = 0L)
    val wdSparql = WDSparql(instanceItems)
    val logger: Logger = LoggerFactory.getLogger("problematic_articles")

    var statements: List<Statement> = listOf()

    wdSparql.selectQuery(
        """
        SELECT DISTINCT ?id ?taxo ?reference

        WHERE {
            ?id          wdt:P235 ?inchiKey;
                         p:P703 ?pp703.
            ?pp703       prov:wasDerivedFrom ?derived;
                         ps:P703 ?taxo.
            ?derived     pr:P248 ?reference.
            VALUES ?reference { <http://www.wikidata.org/entity/Q104415021> }
        } 
        """.trimIndent()
    ) { result ->
        statements = result.map { bindingSet ->
            Statement(
                bindingSet.getValue("id").stringValue().replace("http://www.wikidata.org/entity/", ""),
                bindingSet.getValue("taxo").stringValue().replace("http://www.wikidata.org/entity/", ""),
                bindingSet.getValue("reference").stringValue().replace("http://www.wikidata.org/entity/", ""),
            )
        }
    }

    logger.info("We have ${statements.size} to go over.")

    publisher.connect()
    statements.groupBy { it.compoundId }.map { (compoundId, listOfqueryStatement) ->
        val document = publisher.fetcher?.getEntityDocument(compoundId) ?: return@map
        listOfqueryStatement.map { queryStatement ->
            if (document is ItemDocument) {
                val documentStatements = document.allStatements.iterator().asSequence().toList().filter {
                    it.mainSnak.propertyId.id == "P703"
                }
                    .filter {
                        ((it.mainSnak as ValueSnak).value as ItemIdValue).id == queryStatement.taxonId
                    } // filter for the right taxon
                    .filter { // filter for the statement that contain our reference
                        it.references.any {
                            it.snakGroups.filter { it.property.id == "P248" }.any {
                                it.any {
                                    ((it as ValueSnak).value as ItemIdValue).id == queryStatement.referenceId
                                }
                            }
                        }
                    }
                logger.info("So we have ${documentStatements.size} statements to process for that document")
                // If we have only one ref, we kill the full statement, if not we just kill the ref
                documentStatements.map {

                    if (it.references.size == 1) {
                        publisher.editor?.updateStatements(
                            document.entityId,
                            listOf(), // adds
                            listOf(it), // deletes
                            "Cleaning up my mistakes",
                            listOf()
                        ) ?: throw RuntimeException("Editor is not working!")
                        logger.info(" I deleted this statement")
                    } else {
                        val newRefs = it.references.filter {
                            it.snakGroups.filter { it.property.id == "P248" }.any {
                                it.any {
                                    ((it as ValueSnak).value as ItemIdValue).id != queryStatement.referenceId
                                }
                            }
                        }

                        val newStatement = StatementBuilder.forSubjectAndProperty(
                            ItemIdValue.NULL, instanceItems.foundInTaxon
                        ).withId(it.statementId)
                            .withValue(it.value)
                            .withReferences(newRefs).build()
                        publisher.editor?.updateStatements(
                            document.entityId,
                            listOf(newStatement), // adds (we are overwriting it, no need to delete!)
                            listOf(), // deletes
                            "Cleaning up my mistakes, deleting a mismatched article",
                            listOf()
                        ) ?: throw RuntimeException("Editor is not working!")
                        logger.info(" I delete only the matching ref")
                    }
                }
            }
        }
    }
    publisher.disconnect()
}
