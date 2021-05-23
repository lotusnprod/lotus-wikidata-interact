/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wikidata.download.rdf.vocabulary

import org.eclipse.rdf4j.model.impl.SimpleValueFactory

object Wikidata {
    private val simpleValueFactory = SimpleValueFactory.getInstance()
    fun wdt(localName: String) = simpleValueFactory.createIRI(wdtPrefix, localName)
    fun wd(localName: String) = simpleValueFactory.createIRI(wdPrefix, localName)
    fun p(localName: String) = simpleValueFactory.createIRI(pPrefix, localName)
    fun ps(localName: String) = simpleValueFactory.createIRI(psPrefix, localName)

    /**
     * PREFIX wd: <>
     PREFIX wdt: <>
     PREFIX wikibase: <>
     PREFIX p: <>
     PREFIX prov: <>
     PREFIX ps: <>
     PREFIX pq: <>
     PREFIX pr: <>
     */
    const val wdPrefix = "http://www.wikidata.org/entity/"
    const val wdtPrefix = "http://www.wikidata.org/prop/direct/"
    const val wikibasePrefix = "http://wikiba.se/ontology#"
    const val pPrefix = "http://www.wikidata.org/prop/"
    const val provPrefix = "http://www.w3.org/ns/prov#"
    const val psPrefix = "http://www.wikidata.org/prop/statement/"
    const val pqPrefix = "http://www.wikidata.org/prop/qualifier/"
    const val prPrefix = "http://www.wikidata.org/prop/reference/"

    object Properties {
        val instanceOf = wdt("P31")
    }
}
