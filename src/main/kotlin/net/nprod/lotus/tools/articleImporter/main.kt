/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 * This is a program meant to import CrossREF references into wikidata
 */

package net.nprod.lotus.tools.articleImporter

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.crossref.CrossRefConnector
import net.nprod.konnector.crossref.OfficialCrossRefAPI
import net.nprod.lotus.wdimport.wd.InstanceItems

data class AuthorInfo(
    val ORCID: String?,
    val givenName: String,
    val familyName: String
) {
    val fullName
        get() = "$givenName $familyName"
}

@KtorExperimentalAPI
fun main() {
    val connector = CrossRefConnector(OfficialCrossRefAPI())
    val doi = "10.1021/acs.jmedchem.5B01009"

    try {
        val output = connector.workFromDOI(doi)
        assert(output.status == "OK")
        val message = output.message ?: throw RuntimeException("No info from CrossREF")
        val worktype = message.type
        val title = message.title?.first()
        val issn = message.ISSN?.first()
        val date = message.created?.datetime
        val issue = message.issue
        val volume = message.volume
        val page = message.page
        val doiRetrieved = message.DOI
        val authors = message.author?.map {
            AuthorInfo(
                ORCID = it.ORCID?.split("/")?.last(),
                givenName = it.given ?: "",
                familyName = it.family ?: ""
            )
        } ?: listOf()

        val source = "http://api.crossref.org/works/$doiRetrieved"

        println("Source: $source")
        println("Work type: $worktype")
        println("Title: $title")
        println("ISSN: $issn")
        println("Publication Date: $date")
        println("Issue: $issue")
        println("Volume: $volume")
        println("Pages: $page")
        println("DOI: $doiRetrieved")
        println("Authors: ${authors.map { it.fullName }}")


        val instanceOf = InstanceItems::scholarlyArticle

        // check type, if ok scholarly article, if not, we just put `publication`
        // cut title at 249
        // description "scholarly article published on %d %B %Y "  convert date of course

        // lookup authors if they have ORCID
        // lookup papers if they have DOI (this we have)

        // validate entries (title not empty, publication date exists)

        // Reference is a combination of statements with:
        //  add external ID (DOI)  `DOI` doi
        //  Reference URL (doi?)   `reference URL` source
        //  Stated in CROSSREF     `stated in` `CROSSREF`
        //  Retrieved datetime `retrieved` TODO: Create the WikiData Time type

        // `title` is monolingualtext
        // <article> `publication date` …
        //           `published in` …  (get the Wikidata ID of the item from the ISSN)
        //           `volume` string
        //           `page(s)` string
        //           `issue`  string

        // Authors statements (only if the article is new, if not we would overwrite or duplicate entries)
        // authors is a `series ordinal`
        // of either
        //    `author` if we have a wikidata id
        //    `author name string`if we have only the text name
    } catch (e: net.nprod.konnector.commons.NonExistentReference) {
        println("Could not find that work DOI=$doi")
    }
}