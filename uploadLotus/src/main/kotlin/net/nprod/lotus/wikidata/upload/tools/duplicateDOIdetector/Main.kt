/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.tools.duplicateDOIdetector

import net.nprod.lotus.helpers.GZIPReader
import net.nprod.lotus.importer.parseTSVFile
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.query.WDKT
import java.io.File

fun main(args: Array<String>) {
    val fileName = "args[1]"
    val fileReader = try {
        GZIPReader(fileName).bufferedReader
    } catch (e: java.util.zip.ZipException) {
        e.localizedMessage // To say we swallowed it, not an issue here
        File(fileName).bufferedReader()
    }
    val file = parseTSVFile(fileReader)

    fileReader.close()

    val dois = mutableSetOf<String>()

    file?.map {
        val doi = it.getString("referenceCleanedDoi")
        dois.add(doi)
    }
    println("We have ${dois.size} unique DOIs")
    println("We will now search for those with duplicates")
    val wdkt = WDKT()
    val counts = dois.map {
        it to wdkt.searchForPropertyValue(MainInstanceItems.doi, it).query.searchinfo.totalhits
    }.filter { it.second > 1 }
    println("Here is the list of duplicates")
    println(counts)
}
