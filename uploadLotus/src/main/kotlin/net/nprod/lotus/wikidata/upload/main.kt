/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload

import net.nprod.lotus.wikidata.upload.jobs.LotusProcessRaw
import net.nprod.lotus.wikidata.upload.jobs.LotusRaw
import net.nprod.lotus.wikidata.upload.jobs.UnivocityBasedReader
import net.nprod.lotus.wikidata.upload.jobs.WikiDataWriter
import kotlin.time.ExperimentalTime

//import org.springframework.boot.runApplication

//@Suppress("SpreadOperator")
@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    //runApplication<LotusImporter>(*args)
    println("Hello!")
    val tsvReader = UnivocityBasedReader<LotusRaw> {
        LotusRaw.fromRecord(it)
    }

    val processor = LotusProcessRaw()
    val wdWriter = WikiDataWriter()

    //tsvReader.maximalNumber = 1200
    tsvReader.skip = 0
    tsvReader.open("../data/test_2021-11-24.tsv")
    val list = tsvReader.read()
    println("Processing ${list.size} entries.")
    val processed = processor.process(list)

    wdWriter.write(listOf(processed))

    println("Done!")

    tsvReader.close()
}
