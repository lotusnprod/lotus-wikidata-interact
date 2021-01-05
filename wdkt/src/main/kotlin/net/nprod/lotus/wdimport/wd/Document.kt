/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.exceptions.UnhandledReferencedStatementType
import net.nprod.lotus.wdimport.wd.models.WDResolvedQualifier
import net.nprod.lotus.wdimport.wd.models.statements.IReferencedStatement
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedRemoteItemStatement
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedValueStatement
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Reference
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.datamodel.interfaces.Value

/**
 * Type safe builder or DSL
 */

fun newDocument(name: String, id: ItemIdValue? = null, f: ItemDocumentBuilder.() -> Unit): ItemDocument {
    val builder = ItemDocumentBuilder.forItemId(id ?: ItemIdValue.NULL)

    if (name != "") builder.withLabel(name, "en")
    builder.apply(f)

    return builder.build()
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
        referencedStatement.preQualifiers.forEach { (property, value) ->
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
    referencedStatement: IReferencedStatement,
    instanceItems: InstanceItems
) {
    val resolvedReferencedStatement = when (referencedStatement) {
        is ReferencedValueStatement -> referencedStatement
        is ReferencedRemoteItemStatement -> referencedStatement.resolveToReferencedValueStatetement(instanceItems)
        else -> throw UnhandledReferencedStatementType("Unknown ReferencedStatement type")
    }
    statement(subject, resolvedReferencedStatement, instanceItems)
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

@Suppress("LongParameterList")
fun newStatement(
    property: PropertyIdValue,
    subject: ItemIdValue? = null,
    existingId: String? = null,
    value: Value,
    references: Collection<Reference>,
    qualifiers: Collection<WDResolvedQualifier>
): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(subject ?: ItemIdValue.NULL, property)
        .withValue(value).also { statement ->
            existingId?.let { statement.withId(it) }
        }

    references.forEach { statement.withReference(it) }
    qualifiers.forEach { statement.withQualifierValue(it.property, it.value) }

    return statement.build()
}
