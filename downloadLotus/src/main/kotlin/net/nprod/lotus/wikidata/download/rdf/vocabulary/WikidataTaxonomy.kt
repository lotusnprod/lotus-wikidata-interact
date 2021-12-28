/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.rdf.vocabulary

object WikidataTaxonomy {
    object Properties {
        val taxonName = Wikidata.wdt("P225")
        val taxonRank = Wikidata.wdt("P105")
        val parentTaxon = Wikidata.wdt("P171")
        val algaeId = Wikidata.wdt("P1348")
        val irmngId = Wikidata.wdt("P5055")
        val gbifId = Wikidata.wdt("P846")
        val ncbiId = Wikidata.wdt("P685")
        val otlId = Wikidata.wdt("P9157")
        val wfoId = Wikidata.wdt("P7715")
        val wormsId = Wikidata.wdt("P850")
        val parentTaxonChain = "<" + Wikidata.p("P171") + ">/<" + Wikidata.ps("P171") + ">"
    }
    val taxon = Wikidata.wd("Q16521")
}
