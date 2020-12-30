/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.models.ReferencedRemoteItemStatement
import net.nprod.lotus.wdimport.wd.models.ReferencedStatement
import net.nprod.lotus.wdimport.wd.models.ReferencedValueStatement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Reference
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.datamodel.interfaces.Value
import kotlin.reflect.KProperty1

/**
 * A qualifier that can be added to a statement.
 */
data class Qualifier(
    val property: KProperty1<InstanceItems, PropertyIdValue>,
    val value: Value
)

/**
 * Type safe builder or DSL
 */
@Suppress("unused")
fun newReference(f: (ReferenceBuilder) -> Unit): Reference {
    val reference = ReferenceBuilder.newInstance()
    reference.apply(f)
    return reference.build()
}

fun newDocument(name: String, id: ItemIdValue? = null, f: ItemDocumentBuilder.() -> Unit): ItemDocument {
    val builder = ItemDocumentBuilder.forItemId(id ?: ItemIdValue.NULL)

    if (name != "") builder.withLabel(name, "en")
    builder.apply(f)

    return builder.build()
}

@Suppress("unused")
fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: String): Unit =
    this.propertyValue(property, Datamodel.makeStringValue(value))

fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: Value) {
    this.withPropertyValue(
        property,
        value
    )
}

/**
 * This is used to create a direct statement
 */
fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    property: PropertyIdValue,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
): ItemDocumentBuilder =
    this.withStatement(newStatement(property, subject, value, f))

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    referencedStatement: ReferencedValueStatement,
    instanceItems: InstanceItems
) {
    val statement = newStatement(
        referencedStatement.property.get(instanceItems),
        subject,
        referencedStatement.value
    ) { statementBuilder ->
        referencedStatement.preReferences.forEach {
            statementBuilder.withReference(it.build(instanceItems))
        }
        referencedStatement.qualifiers.forEach { (property, value) ->
            statementBuilder.withQualifierValue(property.get(instanceItems), value)
        }
    }

    this.withStatement(statement)
}

/**
 * Used to resolve when we have a ReferencedRemoteItemStatement
 */
fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    referencedStatement: ReferencedStatement,
    instanceItems: InstanceItems
) {
    val resolvedReferenceableStatement = when (referencedStatement) {
        is ReferencedValueStatement -> referencedStatement
        is ReferencedRemoteItemStatement -> referencedStatement.resolveToReferencedValueStatetement(instanceItems)
        else -> throw RuntimeException("Unknown ReferencedStatement type")
    }
    statement(subject, resolvedReferenceableStatement, instanceItems)
}

fun newStatement(
    property: PropertyIdValue,
    subject: ItemIdValue? = null,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(subject ?: ItemIdValue.NULL, property)
        .withValue(value)
        .apply(f)
    return statement.build()
}

fun newStatement(
    property: PropertyIdValue,
    subject: ItemIdValue? = null,
    value: Value,
    references: Collection<Reference>
): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(subject ?: ItemIdValue.NULL, property)
        .withValue(value)
    references.forEach { statement.withReference(it) }
    return statement.build()
}
