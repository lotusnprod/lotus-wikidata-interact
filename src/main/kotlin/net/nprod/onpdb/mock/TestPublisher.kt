package net.nprod.onpdb.mock

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.Publisher
import net.nprod.onpdb.wdimport.wd.Resolver
import net.nprod.onpdb.wdimport.wd.models.Publishable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.PropertyDocumentBuilder
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.*
import org.wikidata.wdtk.dumpfiles.DumpProcessingController
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.rdf.RdfSerializer
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class TestItemIdValue(private val prefix: String, private val value: String): ItemIdValue {
    override fun <T : Any?> accept(valueVisitor: ValueVisitor<T>?): T {
        TODO("Not yet implemented")
    }

    override fun getIri(): String {
        return "$prefix$value"
    }

    override fun getEntityType(): String {
        return EntityIdValue.ET_ITEM
    }

    override fun getId(): String {
        return value
    }

    override fun getSiteIri(): String {
        return prefix
    }
}

class TestPublisher(override val instanceItems: InstanceItems, val repository: Repository) : Resolver, Publisher {
    private val site = InstanceItems::wdURI.get(instanceItems)
    private var counter = 0
    private val logger: Logger = LogManager.getLogger(this::class.java)

    val dumpProcessingController = DumpProcessingController(
        "wikidatawiki"
    )

    val sites: Sites?

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

    override fun newProperty(name: String, description: String): PropertyIdValue {
        logger.debug("Trying to add a property name: $name ; description: $description")
        val doc = PropertyDocumentBuilder.forPropertyIdAndDatatype(PropertyIdValue.NULL, DatatypeIdValue.DT_STRING)
            .withLabel(Datamodel.makeMonolingualTextValue(name, "en"))
            .withDescription(Datamodel.makeMonolingualTextValue(description, "en")).build()
        return doc.entityId
    }

    override fun publish(publishable: Publishable, summary: String): ItemIdValue {
        if (publishable.published) return publishable.id
        logger.debug("Trying to add the publishable: $publishable with a summary $summary")
        val entityId = ItemIdValueImpl.fromId("Q${counter.toString().padStart(8, '0')}", site)
        counter++
        val doc = publishable.document(instanceItems, entityId as ItemIdValue)

        logger.debug(doc.entityId)
        val conn = repository.connection

        val stream = ByteArrayOutputStream()
        val serializer = RdfSerializer(
            RDFFormat.NTRIPLES,
            stream,
            sites,
            PropertyRegister.getWikidataPropertyRegister()
        )
        // Serialize simple statements (and nothing else) for all items
        serializer.tasks = (RdfSerializer.TASK_ITEMS
                or RdfSerializer.TASK_SIMPLE_STATEMENTS)

        // Run serialization
        serializer.open()
        serializer.processItemDocument(doc)
        serializer.close()

        conn.add(Rio.parse(ByteArrayInputStream(stream.toByteArray()), "", RDFFormat.NTRIPLES))

        val id = doc.entityId
        publishable.published(id)
        return id
    }
}