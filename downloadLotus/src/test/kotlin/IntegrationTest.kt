/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wikidata.download

import net.nprod.lotus.wikidata.download.modes.mirror.addEntriesFromConstruct
import net.nprod.lotus.wikidata.download.modes.mirror.getAllTaxRanks
import net.nprod.lotus.wikidata.download.modes.mirror.getIRIsAndTaxaIRIs
import net.nprod.lotus.wikidata.download.modes.mirror.getTaxaParentIRIs
import net.nprod.lotus.wikidata.download.rdf.RDFRepository
import net.nprod.lotus.wikidata.download.sparql.LOTUSQueries
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class IntegrationTest {
    @Test
    @Order(1)
    fun `Query Wikidata for 10 triplets taxon-compound-reference, must get more than 10`() {
        val fullEntries = sparqlRepository.addEntriesFromConstruct(LOTUSQueries.queryCompoundTaxonRef + "\nLIMIT 10")
        rdfRepository.repository.connection.use { it.add(fullEntries) }
        assert(fullEntries.size > 10)
    }

    @Test
    @Order(2)
    fun `Query local data for all the ids, check that we have both taxa and entries`() {
        val (irisToMirror, taxasToParentMirror) = rdfRepository.repository.getIRIsAndTaxaIRIs()
        assert(irisToMirror.isNotEmpty())
        assert(taxasToParentMirror.isNotEmpty())
    }

    @Test
    fun `Get Parents of a specific taxa`() {
        val iri = sparqlRepository.valueFactory.createIRI("http://www.wikidata.org/entity/Q1549545")
        val taxaParents = sparqlRepository.getTaxaParentIRIs(setOf(iri))
        assert(taxaParents.size > 5)
    }

    @Test
    fun `Get all the taxa ranks`() {
        assert(sparqlRepository.getAllTaxRanks(LOTUSQueries.queryTaxoRanksInfo + "\nLIMIT 10").size == 10)
    }

    /*@Test
    fun `Get everything about something`() = runBlocking {
        val iri = sparqlRepository.valueFactory.createIRI("http://www.wikidata.org/entity/Q1549545")
        assert(sparqlRepository.getEverythingAbout(setOf(iri), channel = Channel()).isNotEmpty())
    }*/

    companion

    object {
        private lateinit var rdfRepository: RDFRepository
        private lateinit var sparqlRepository: SPARQLRepository

        @BeforeAll
        @JvmStatic
        fun setup(@TempDir tempDir: Path) {
            sparqlRepository = SPARQLRepository("https://query.wikidata.org/sparql")
            rdfRepository = RDFRepository(tempDir.toFile())
        }
    }
}
