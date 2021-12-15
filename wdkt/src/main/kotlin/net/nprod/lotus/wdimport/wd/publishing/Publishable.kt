/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.publishing

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.WDResolvedQualifier
import net.nprod.lotus.wdimport.wd.models.statements.IReferencedStatement
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedRemoteItemStatement
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedValueStatement
import net.nprod.lotus.wdimport.wd.newDocument
import net.nprod.lotus.wdimport.wd.newStatement
import net.nprod.lotus.wdimport.wd.statement
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.implementation.ReferenceImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Reference
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.datamodel.interfaces.Value
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import kotlin.reflect.KProperty1

/**
 * Maximum label length in Wikidata
 */
const val MAXIMUM_LABEL_LENGTH: Int = 249

/**
 * Raised when an element should have been published before running that function
 */
class ElementNotPublishedException(msg: String) : Exception(msg)

typealias RemoteItem = KProperty1<InstanceItems, ItemIdValue>
typealias RemoteProperty = KProperty1<InstanceItems, PropertyIdValue>

/**
 * if statement is null, we just use the info for a new statement
 */
data class UpdatableStatement(
    val statement: Statement,
    val newReferences: List<Reference>,
    val newQualifiers: List<WDResolvedQualifier>,
    val updateOnly: Boolean
)

/**
 * A publishable is a document and its properties.
 * This allows to create documents and update them.
 *
 * @property label label of this entry
 * @property type used for `instance of` it is a RemoteItem that will be calculated at statement build time
 */
abstract class Publishable {
    private val logger: Logger = LogManager.getLogger(Publishable::class.qualifiedName)

    private var _id: ItemIdValue? = null

    abstract var label: String
    abstract var type: RemoteItem

    /**
     * Was that entry published, this can be changed either when the entry has been found in WikiData
     * or when it has been sent.
     */
    var published: Boolean = false

    /**
     * The ID of that entry, only exists if the entry.
     *
     * Trying to access that value on an unpublished object will raise an ElementNotPublishedException
     */
    val id: ItemIdValue
        get() = _id
            ?: throw ElementNotPublishedException(
                "This element has not been published yet or failed to get published."
            )

    /**
     * The statements that are not translated yet
     */
    val preStatements: MutableSet<IReferencedStatement> = mutableSetOf()

    /**
     * Sets the ID
     *
     * @param id new id of this object
     */
    fun publishedAs(id: ItemIdValue) {
        _id = id
        published = true
    }

    /**
     * Return a list of all the Referenceable statements of this object
     */
    abstract fun dataStatements(): List<IReferencedStatement>

    /**
     * Generate a document for that entry.
     * It will use the internal id if it exists already
     */
    fun document(instanceItems: InstanceItems, subject: ItemIdValue? = null): ItemDocument {
        preStatements.addAll(dataStatements())
        logger.info("We upserted the document and we added ${dataStatements().size} statements")

        // We are limited to names < 250 characters
        val legalName = if (label.length < MAXIMUM_LABEL_LENGTH) {
            label
        } else {
            ""
        }

        return newDocument(legalName, subject ?: _id) {
            statement(subject ?: _id, instanceItems.instanceOf, type.get(instanceItems))
            logger.info("Creating a new document - Subject: ${subject ?: _id} - type: ${type.get(instanceItems)}")
            // We construct the statements according to this instanceItems value
            preStatements.forEach { referenceableStatement ->
                statement(subject ?: _id, referenceableStatement, instanceItems)
            }
            preStatements.clear()
        }
    }

    /**
     * Generate statements for updating
     */
    fun listOfResolvedStatements(
        fetcher: WikibaseDataFetcher?,
        instanceItems: InstanceItems
    ): List<Statement> {
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

        val existingPropertyValueCoupleToReferencesIds: Map<String, Map<Value, Statement>> =
            existingStatements.map {
                it.mainSnak.propertyId.id to it
            }.groupBy { it.first }.map {
                it.key to it.value.map { it.second.value to it.second }.toMap()
            }.toMap()

        return preStatements.mapNotNull { statement ->
            val value: Value? = when (statement) {
                is ReferencedValueStatement -> statement.value
                is ReferencedRemoteItemStatement -> statement.value.get(instanceItems)
                else -> null // The statement is currently invalid if we do not know how to handle its values
            }

            value?.let {
                constructStatement(statement, value, instanceItems, existingPropertyValueCoupleToReferencesIds)
            }
        }.filter { stmt ->
            stmt.statementId !in existingStatements.map { it.statementId } || stmt.references.any {
                val existingReferences =
                    (existingStatements.firstOrNull { stmt.statementId == it.statementId })?.references?.map { it.hash }
                        ?: listOf()
                it.hash !in existingReferences
            }
        }
    }

    /**
     * We need to find a cleaner and safer way to do that, for now it will crash the bot on purpose to make sure nothing
     * bad happens if anything is not of the expected type.
     */
    fun Reference.forceGetStatedInValue(): List<Value> = (this as ReferenceImpl).snaks.filter { it.key == "P248" }
        .flatMap { it.value.map { (it as ValueSnak).value } }

    /**
     * This is where the magic happens and the new statements are compared to existing statements
     *
     */
    private fun constructStatement(
        statement: IReferencedStatement,
        newStatementValue: Value,
        instanceItems: InstanceItems,
        existingPropertyValueCoupleToReferences: Map<String, Map<Value, Statement>>
    ): Statement? {

        val newStatementProperty: PropertyIdValue = statement.property.get(instanceItems)

        val references = statement.preReferences.map { it.build(instanceItems) }

        val existingProperty = existingPropertyValueCoupleToReferences[newStatementProperty.id]
        val existingStatement = existingProperty?.let { existingValueToReferences ->
            existingValueToReferences[newStatementValue]
        }

        val existingSetOfReferences =
            existingStatement?.references?.map { it.forceGetStatedInValue() }?.toSet() ?: setOf()
        // val existingSetOfQualifiers =
        //     existingStatement?.qualifiers?.map { it.property }?.toSet() ?: setOf()

        // Anything that is not in the existing set should be a new reference
        val newReferences = references.filterNot {
            it.forceGetStatedInValue() in existingSetOfReferences
        }

        val existingReferencesToPortOver = (existingStatement?.references ?: listOf()).filterNot { existingRef ->
            newReferences.any { it.forceGetStatedInValue() == existingRef.forceGetStatedInValue() }
        }

        // val newQualifiers = statement.preQualifiers.filterNot {
        //     it.property.get(instanceItems) in existingSetOfQualifiers
        // }.map { WDResolvedQualifier.fromQualifier(it, instanceItems) }

        // We do not try to add or modify a non overridable statement
        existingProperty?.let { if (!statement.overwritable) return null }

        /**
         * We either create a new statement, or use the existing one, that's because the process is different for them
         */

        val qualifiers = statement.preQualifiers.map {
            WDResolvedQualifier.fromQualifier(it, instanceItems)
        }

        return newStatement(
            newStatementProperty,
            id,
            existingStatement?.statementId,
            newStatementValue,
            existingReferencesToPortOver + newReferences,
            qualifiers,
        )
    }

    /**
     * A function that will find an object matching the entries already existing
     */
    abstract fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): Publishable

    /**
     * Add a property to that object
     *
     * @param remoteProperty This is a property that can be calculated at the statements build time
     * @param value value for that property
     * @param f function that will be applied on the statement built
     */
    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferencedValueStatement.() -> Unit = {}) {
        val refStatement = ReferencedValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}
