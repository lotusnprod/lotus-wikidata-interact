// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.implementation.ReferenceImpl
import org.wikidata.wdtk.datamodel.interfaces.*
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import kotlin.reflect.KProperty1

class ElementNotPublishedError(msg: String) : Exception(msg)

typealias RemoteItem = KProperty1<InstanceItems, ItemIdValue>
typealias RemoteProperty = KProperty1<InstanceItems, PropertyIdValue>

/**
 * A publishable is a document and its properties.
 * This allows to create documents and update them.
 */
abstract class Publishable {
    private val logger: Logger = LogManager.getLogger(Publishable::class.qualifiedName)

    private var _id: ItemIdValue? = null

    abstract var name: String
    abstract var type: RemoteItem

    var published: Boolean = false

    val id: ItemIdValue
        get() = _id
            ?: throw ElementNotPublishedError("This element has not been published yet or failed to get published.")

    val preStatements: MutableSet<ReferenceableStatement> = mutableSetOf()

    fun published(id: ItemIdValue) {
        _id = id
        published = true
    }

    abstract fun dataStatements(): List<ReferenceableStatement>

    /**
     * Generate a document for that entry.
     * It will use the internal id if it exists already
     */
    fun document(instanceItems: InstanceItems, subject: ItemIdValue? = null): ItemDocument {
        preStatements.addAll(dataStatements())
        println("We upserted the document and we added ${dataStatements().size} statements")

        // We are limited to names < 250 characters
        val legalName = if (name.length < 250) {
            name
        } else {
            ""
        }

        return newDocument(legalName, subject ?: _id) {
            statement(subject ?: _id, instanceItems.instanceOf, type.get(instanceItems))
            logger.info("Creating a new document - Subject: ${subject ?: _id} - type: ${type.get(instanceItems)}")
            // We construct the statements according to this instanceItems value
            preStatements.forEach { refStat ->
                when (refStat) {
                    is ReferencableValueStatement -> {
                        logger.info(" Adding a ReferencableValueStatement - refStat: ${refStat.property.name} = ${refStat.value}")
                        statement(subject ?: _id, refStat, instanceItems)
                    }
                    is ReferenceableRemoteItemStatement -> {
                        logger.info(" Adding a ReferencableRemoteItemStatement - refStat: ${refStat.property.name} = ${refStat.value}")
                        statement(subject ?: _id, refStat, instanceItems)
                    }
                }
            }
            preStatements.clear()
        }
    }

    /**
     * Generate statements for updating
     */
    fun listOfStatementsForUpdate(fetcher: WikibaseDataFetcher?, instanceItems: InstanceItems): List<Statement> {
        // Add the data statements
        preStatements.addAll(dataStatements())
        // If we have a fetcher, we take a dump of that entry to make sure we are not modifying existing entries
        // We generate a list of all the properties on the existing object (if it exist)
        val existingStatements = fetcher?.let {
            val id = _id?.id
            if (id != null) {
                val doc = it.getEntityDocument(id)
                if (doc is ItemDocument) {
                    doc.allStatements.iterator().asSequence().toList() // so we have types
                } else listOf()
            } else listOf()
        } ?: listOf()

        val existingPropertyValueCoupleToReferencesIds: Map<String, Map<Value, Pair<Statement, MutableList<Reference>>>> =
            existingStatements.map {
                it.mainSnak.propertyId.id to it
            }.groupBy { it.first }.map {
                it.key to it.value.map { it.second.value to Pair(it.second, it.second.references) }.toMap()
            }.toMap()

        return preStatements.mapNotNull { statement ->
            constructStatement(statement, instanceItems, existingPropertyValueCoupleToReferencesIds)
        }
    }

    /**
     * We need to find a cleaner and safer way to do that, for now it will crash the bot on purpose to make sure nothing bad
     * happens if anything is not of the expected type.
     */
    fun Reference.forceGetStatedInValue(): List<Value> = (this as ReferenceImpl).snaks.filter { it.key == "P248" }
        .flatMap { it.value.map { (it as ValueSnak).value } }


    /**
     * This is where the magic happens and the new statements are compared to existing statements
     *
     */
    private fun constructStatement(
        statement: ReferenceableStatement,
        instanceItems: InstanceItems,
        existingPropertyValueCoupleToReferences: Map<String, Map<Value, Pair<Statement, List<Reference>>>>
    ): Statement? {
        val newStatementValue: Value = when (statement) {
            is ReferencableValueStatement -> statement.value
            is ReferenceableRemoteItemStatement -> statement.value.get(instanceItems)
            else -> null
        } ?: return null  // The statement is currently invalid if we do not know how to handle its values

        val newStatementProperty: PropertyIdValue = statement.property.get(instanceItems)

        val builtStatements = statement.preReferences.map { it.build(instanceItems) }

        val existingProperty = existingPropertyValueCoupleToReferences[newStatementProperty.id]
        val (existingStatement, existingReferences) = existingProperty?.let { existingValueToReferences ->
            existingValueToReferences[newStatementValue]
        } ?: Pair(null, null)

        existingProperty?.let { if (!statement.overwritable) return null } // We do not try to add or modify a non overridable statement

        val existingSet = existingReferences?.map { it.forceGetStatedInValue() }?.toSet() ?: setOf()

        // Anything that is not in the existing set should be a new reference
        val newReferences = builtStatements.filterNot { it.forceGetStatedInValue() in existingSet }

        // If we have an existing statement we just add it
        existingStatement?.references?.addAll(newReferences)

        return existingStatement ?: newStatement(newStatementProperty, id, newStatementValue, newReferences)
    }

    abstract fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): Publishable

    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferencableValueStatement.() -> Unit = {}) {
        val refStatement = ReferencableValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}
