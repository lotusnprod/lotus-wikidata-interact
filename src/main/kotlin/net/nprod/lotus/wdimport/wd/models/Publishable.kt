package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.newDocument
import net.nprod.lotus.wdimport.wd.newStatement
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.statement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
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

            // We construct the statements according to this instanceItems value
            preStatements.forEach { refStat ->
                when (refStat) {
                    is ReferencableValueStatement -> statement(subject ?: _id, refStat, instanceItems)
                    is ReferenceableRemoteItemStatement -> statement(subject ?: _id, refStat, instanceItems)
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
        // We generate a list of all the properties' ids
        val propertiesIDs = fetcher?.let {
            val id = _id?.id
            if (id != null) {
                val doc = it.getEntityDocument(id)
                if (doc is ItemDocument) {
                    val statements = doc.allStatements.iterator().asSequence().toList()
                    statements.map { statement ->
                        statement.mainSnak.propertyId.id
                    }
                } else {
                    listOf()
                }
            } else {
                listOf()
            }
        } ?: listOf()
        println("Existing ids $propertiesIDs")
        println("Existing statemts ${preStatements.map{it.property.get(instanceItems).id}}")
        return preStatements.filter { statement ->  // Filter statements that already exist and are not overwritable
            statement.overwriteable || !propertiesIDs.contains(statement.property.get(instanceItems).id)
        }.map { statement ->
            when (statement) {
                is ReferencableValueStatement -> newStatement(
                    statement.property.get(instanceItems),
                    id,
                    statement.value
                ) { statementBuilder ->
                    statement.preReferences.forEach {
                        statementBuilder.withReference(it.build(instanceItems))
                    }
                }
                is ReferenceableRemoteItemStatement -> newStatement(
                    statement.property.get(instanceItems),
                    id,
                    ReferencableValueStatement(
                        statement.property,
                        statement.value.get(instanceItems),
                        statement.preReferences
                    ).value
                ) { statementBuilder ->
                    statement.preReferences.forEach {
                        statementBuilder.withReference(it.build(instanceItems))
                    }
                }
                else -> throw Exception("Unhandled statement type.")
            }
        }
    }

    abstract fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): Publishable

    fun addProperty(remoteProperty: RemoteProperty, value: Value, f: ReferencableValueStatement.() -> Unit = {}) {
        val refStatement = ReferencableValueStatement(remoteProperty, value).apply(f)
        preStatements.add(refStatement)
    }

    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferencableValueStatement.() -> Unit = {}) {
        val refStatement = ReferencableValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}
