/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.publishing.mock

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Resolver
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.PropertyDocumentBuilder
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Sites
import org.wikidata.wdtk.datamodel.interfaces.StringValue
import org.wikidata.wdtk.dumpfiles.DumpProcessingController
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.rdf.RdfSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Tried to add an empty property
 */
class EmptyPropertyNotAllowedException(
    override val message: String,
) : RuntimeException()

/**
 * A Test publisher to test the publishing system
 */
class TestPublisher(
    override val instanceItems: InstanceItems,
    private val repository: Repository?,
) : Resolver,
    IPublisher {
    private val logger: Logger = LogManager.getLogger("Test Publisher")
    override var newDocuments: Int = 0
    override var updatedDocuments: Int = 0
    private val site = InstanceItems::wdURI.get(instanceItems)
    private var counter = 0

    private val dumpProcessingController =
        DumpProcessingController(
            "wikidatawiki",
        )

    private val sites: Sites?

    init {
        dumpProcessingController.setOfflineMode(false)
        sites = dumpProcessingController.sitesInformation
    }

    override fun connect() {
        logger.info("Connecting")
    }

    override fun disconnect() {
        logger.info("Disconnecting")
    }

    override fun newProperty(
        name: String,
        description: String,
    ): PropertyIdValue {
        logger.debug("Trying to add a property name: $name ; description: $description")
        val doc =
            PropertyDocumentBuilder
                .forPropertyIdAndDatatype(PropertyIdValue.NULL, DatatypeIdValue.DT_STRING)
                .withLabel(Datamodel.makeMonolingualTextValue(name, "en"))
                .withDescription(Datamodel.makeMonolingualTextValue(description, "en"))
                .build()
        return doc.entityId
    }

    override fun publish(
        publishable: Publishable,
        summary: String,
    ): ItemIdValue {
        logger.debug("Trying to add the publishable: $publishable with a summary $summary")

        val entityId = ItemIdValueImpl.fromId("Q${counter.toString().padStart(length = 8, '0')}", site)
        counter++

        if (publishable.published) {
            newDocuments++
        } else {
            updatedDocuments++
        }

        val doc = publishable.document(instanceItems, entityId as ItemIdValue)

        doc.allStatements.asSequence().toList().map {
            when (val value = it.value) {
                is StringValue ->
                    if (value.string == "") {
                        throw EmptyPropertyNotAllowedException("We cannot send an empty property for entry $doc")
                    }
            }
        }
        val conn = repository?.connection

        val stream = ByteArrayOutputStream()
        val serializer =
            RdfSerializer(
                RDFFormat.NTRIPLES,
                stream,
                sites,
                PropertyRegister.getWikidataPropertyRegister(),
            )

        // Serialize simple statements (and nothing else) for all items
        serializer.tasks = (
            RdfSerializer.TASK_ITEMS
                or RdfSerializer.TASK_SIMPLE_STATEMENTS
        )

        // Run serialization
        serializer.open()
        serializer.processItemDocument(doc)
        serializer.close()

        conn?.add(Rio.parse(ByteArrayInputStream(stream.toByteArray()), "", RDFFormat.NTRIPLES))

        val id = doc.entityId
        publishable.publishedAs(id)
        return id
    }
}
