// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.tools.duplicateDOIdetector

import net.nprod.lotus.helpers.GZIPReader
import net.nprod.lotus.helpers.parseTSVFile
import net.nprod.lotus.wdimport.wd.query.WDKT
import java.io.File

fun main() {
    val fileName = "data/manuallyValidated.tsv"
    val fileReader = try {
        GZIPReader(fileName).bufferedReader
    } catch (e: java.util.zip.ZipException) {
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
        it to wdkt.searchDOI(it).query.searchinfo.totalhits
    }.filter { it.second > 1 }
    println("Here is the list of duplicates")
    println(counts)
}
