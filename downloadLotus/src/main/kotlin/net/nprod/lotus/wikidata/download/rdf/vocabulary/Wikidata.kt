/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wikidata.download.rdf.vocabulary

import org.eclipse.rdf4j.model.impl.SimpleValueFactory

object Wikidata {
    private val simpleValueFactory = SimpleValueFactory.getInstance()

    fun wdt(localName: String) = simpleValueFactory.createIRI(WDTPREFIX, localName)

    fun wd(localName: String) = simpleValueFactory.createIRI(WDPREFIX, localName)

    fun p(localName: String) = simpleValueFactory.createIRI(PPREFIX, localName)

    fun ps(localName: String) = simpleValueFactory.createIRI(PSPREFIX, localName)

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
    const val WDPREFIX = "http://www.wikidata.org/entity/"
    const val WDTPREFIX = "http://www.wikidata.org/prop/direct/"
    const val WIKIBASEPREFIX = "http://wikiba.se/ontology#"
    const val PPREFIX = "http://www.wikidata.org/prop/"
    const val PROVPREFIX = "http://www.w3.org/ns/prov#"
    const val PSPREFIX = "http://www.wikidata.org/prop/statement/"
    const val PQPREFIX = "http://www.wikidata.org/prop/qualifier/"
    const val PRPREFIX = "http://www.wikidata.org/prop/reference/"

    object Properties {
        val instanceOf = wdt("P31")
    }
}
