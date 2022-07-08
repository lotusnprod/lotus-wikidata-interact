/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wikidata.download.modes.mirror

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.nprod.lotus.wikidata.download.rdf.RDFRepository
import net.nprod.lotus.wikidata.download.sparql.LOTUSQueries
import org.eclipse.rdf4j.common.transaction.IsolationLevels
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.repository.util.Repositories
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun IRI.getIDFromIRI(): String = this.stringValue().split("/").last()

/**
 * Size of the blocks of values for each SPARQL query
 */
const val CHUNK_SIZE = 100
const val LARGE_CHUNK_SIZE = CHUNK_SIZE * 10

fun Repository.addEntriesFromConstruct(query: String = LOTUSQueries.queryCompoundTaxonRef): List<Statement> {
    val list = mutableListOf<Statement>()
    Repositories.graphQuery(this, query) { result ->
        list.addAll(result)
    }
    return list
}

data class Iris(
    val compoundIRIs: Set<IRI>,
    val referenceIRIs: Set<IRI>,
    val taxaIRIs: Set<IRI>
)

fun Repository.getIRIsAndTaxaIRIs(logger: Logger? = null): Iris {
    val compoundIrisToMirror = mutableSetOf<IRI>()
    val refIrisToMirror = mutableSetOf<IRI>()
    val taxaToParentMirror = mutableSetOf<IRI>()
    // We add all the ids to a set so we can mirror them
    var count = 0
    Repositories.tupleQuery(this, LOTUSQueries.queryIdsLocal) { result: TupleQueryResult ->
        result.forEach { bindingSet ->
            val compoundID: IRI = bindingSet.getBinding("compound_id").value as IRI
            val taxonID: IRI = bindingSet.getBinding("taxon_id").value as IRI
            val referenceID: IRI = bindingSet.getBinding("reference_id").value as IRI
            compoundIrisToMirror.add(compoundID)
            refIrisToMirror.add(referenceID)
            taxaToParentMirror.add(taxonID)
            count++
        }
    }

    logger?.info(
        " We found $count LOTUS-like triplets (${compoundIrisToMirror.size} compounds, " +
            " ${refIrisToMirror.size} references ${taxaToParentMirror.size} taxa)"
    )
    return Iris(compoundIrisToMirror, refIrisToMirror, taxaToParentMirror)
}

/**
 * Complete the given set of Taxa IRIs with all the parents IRIs
 *
 * @param taxasToParentMirror All the taxa you want parents off
 * @return A set of IRIs of all the parents
 */
fun Repository.getTaxaParentIRIs(taxasToParentMirror: Set<IRI>): Set<IRI> {
    val newIRIsToMirror = mutableSetOf<IRI>()
    taxasToParentMirror.chunked(CHUNK_SIZE).forEach {
        val listOfTaxa = it.map { "wd:${it.getIDFromIRI()}" }.joinToString(" ")
        val modifiedQuery = LOTUSQueries.queryTaxonParents.replace("%%IDS%%", listOfTaxa)
        Repositories.tupleQuery(this, modifiedQuery) { result: TupleQueryResult ->
            newIRIsToMirror.addAll(
                result.mapNotNull { bindingSet ->
                    when (val value = bindingSet.getBinding("parenttaxon_id").value) {
                        is IRI -> value
                        else -> { // In two cases we have parenttaxon_id being a blank node, we just ignore those two
                            println("Incorrect value $value")
                            null
                        }
                    }
                }
            )
        }
    }
    return newIRIsToMirror
}

fun Repository.getAllTaxRanks(query: String = LOTUSQueries.queryTaxoRanksInfo): List<Statement> {
    val list = mutableListOf<Statement>()
    Repositories.graphQuery(this, query) { result ->
        list.addAll(result)
    }
    return list
}

/**
 * Get all the compounds that have a found in taxon
 */
fun Repository.getAllCompoundsID(query: String = LOTUSQueries.queryCompoundsOfInterest): Set<IRI> =
    mutableSetOf<IRI>().also { set ->
        Repositories.tupleQuery(this, query) { result ->
            set.addAll(result.map { it.getBinding("compound_id").value as IRI })
        }
    }

/**
 * Get all the information about the given IRIs
 *
 * @param iris Collection of IRIs
 * @param f Function that will be executed every chunk (useful for logging)
 */
suspend fun Repository.getEverythingAbout(
    iris: Collection<IRI>,
    query: String,
    chunkSize: Int = CHUNK_SIZE,
    channel: Channel<List<Statement>>,
    f: (Int) -> Unit = { }
) {
    var count = 0
    iris.chunked(chunkSize).map {
        val listOfCompounds = it.map { "wd:${it.getIDFromIRI()}" }.joinToString(" ")
        val compoundQuery = query.replace("%%IDS%%", listOfCompounds)
        var statementList: List<Statement> = listOf()
        Repositories.graphQuery(this, compoundQuery) { result ->
            count += it.size
            statementList = result.map { it }
            f(count)
        }
        channel.send(statementList)
    }
}

/**
 * Get the taxa and refs for the given compound IRIs
 *
 * @param iris Collection of compounds IRIs
 * @param f Function that will be executed every chunk (useful for logging)
 */
suspend fun Repository.getAndLoadTaxaAndRefsAboutGivenCompounds(
    iris: Collection<IRI>,
    chunkSize: Int = CHUNK_SIZE,
    channel: Channel<List<Statement>>,
    f: (Int) -> Unit = {}
): List<Statement> {
    val list = mutableListOf<Statement>()
    val query = LOTUSQueries.queryCompoundTaxonRefModularForCompoundIds
    var count = 0
    iris.chunked(chunkSize).map {
        val listOfCompounds = it.map { "wd:${it.getIDFromIRI()}" }.joinToString(" ")

        val compoundQuery = query.replace("%%IDS%%", listOfCompounds)

        var statementList: List<Statement> = listOf()

        Repositories.graphQuery(this, compoundQuery) { result ->
            statementList = result.map { it }
        }

        count += it.size
        channel.send(statementList)
        f(count)
    }
    return list
}

suspend fun RDFRepository.repositoryWriter(channelStatements: Channel<List<Statement>>) {
    val logger = LoggerFactory.getLogger("repository writer")

    logger.info("Started the writer coroutine")
    return this.repository.connection.use {
        it.isolationLevel = IsolationLevels.READ_UNCOMMITTED
        for (statement in channelStatements) {
            it.add(statement)
        }
    }
}

@Suppress("MagicNumber", "LongMethod")
fun mirror(repositoryLocation: File) = runBlocking<Unit> {
    val logger = LoggerFactory.getLogger("mirror")
    val sparqlRepository = SPARQLRepository("https://query.wikidata.org/sparql")
    val rdfRepository = RDFRepository(repositoryLocation)

    logger.info("Starting in mirroring mode into the repository: $repositoryLocation")

    val channelStatements = Channel<List<Statement>>(CHUNK_SIZE * 100)

    // This is the coroutine that loads the data into the triple store
    val repositoryWriter = launch(Dispatchers.IO) {
        rdfRepository.repositoryWriter(channelStatements)
    }

    launch(Dispatchers.IO) {
        logger.info("Querying Wikidata for the compounds having a found in taxon")
        val compoundsIRIList = sparqlRepository.getAllCompoundsID()

        logger.info("Querying Wikidata for all the triplets taxon-compound-reference and store them")
        var last = 0
        sparqlRepository.getAndLoadTaxaAndRefsAboutGivenCompounds(
            compoundsIRIList,
            channel = channelStatements,
            chunkSize = CHUNK_SIZE
        ) {
            val percentage = 100 * it / compoundsIRIList.size
            if (last != percentage) {
                logger.info(" $percentage%")
                last = percentage
            }
        }

        logger.info("Querying the local data for all the ids we need")
        val localIris = rdfRepository.repository.getIRIsAndTaxaIRIs(logger)

        logger.info("Getting the taxa relations remotely")

        val newTaxaToMirrorIRIs = sparqlRepository.getTaxaParentIRIs(localIris.taxaIRIs)

        logger.info("Getting the taxonomic ranks info")
        channelStatements.send(sparqlRepository.getAllTaxRanks())

        logger.info("Gathering full data about all the taxo")
        val allIRIsTaxo = newTaxaToMirrorIRIs + localIris.taxaIRIs
        sparqlRepository.getEverythingAbout(
            allIRIsTaxo,
            channel = channelStatements,
            query = LOTUSQueries.mirrorQueryForTaxo,
            chunkSize = LARGE_CHUNK_SIZE
        ) { count ->
            logger.info(" $count/${allIRIsTaxo.size} done")
        }

        logger.info("Gathering data about the compounds")
        sparqlRepository.getEverythingAbout(
            localIris.compoundIRIs,
            query = LOTUSQueries.mirrorQueryForCompound,
            chunkSize = LARGE_CHUNK_SIZE,
            channel = channelStatements
        ) { count ->
            logger.info(" $count/${localIris.compoundIRIs.size} done")
        }

        logger.info("Gathering data about the references")
        sparqlRepository.getEverythingAbout(
            localIris.referenceIRIs,
            query = LOTUSQueries.mirrorQueryForReference,
            chunkSize = LARGE_CHUNK_SIZE,
            channel = channelStatements
        ) { count ->
            logger.info(" $count/${localIris.referenceIRIs.size} done")
        }

        logger.info("Finishing sending everything to the local store.")
        channelStatements.close()
    }

    // We wait for the writer to be done
    repositoryWriter.join()
    logger.info("We have ${rdfRepository.repository.connection.use { it.size() }} entries in the local repository")
}
