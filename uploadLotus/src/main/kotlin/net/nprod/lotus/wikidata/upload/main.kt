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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

//import org.springframework.boot.runApplication

//@Suppress("SpreadOperator")
@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("MainThread")
    //runApplication<LotusImporter>(*args)
    logger.info("Hello!")
    val tsvReader = UnivocityBasedReader<LotusRaw> {
        LotusRaw.fromRecord(it)
    }

    //val filePrepath = "../"
    val fileName = args[0]
    val filePath = fileName;

    val wdWriter = WikiDataWriter()

    //tsvReader.maximalNumber = 1200
    tsvReader.skip = 0
    tsvReader.open(filePath)
    val list = tsvReader.read()
    tsvReader.close()
    logger.info("Finished parsing the TSV")
    // Let's chunk to see if we can avoid OOM errors… Currently it needs more than 32GB
    // This may be a bit slower as we loose the cache every time
    list.chunked(100000) { sublist ->
        logger.info("Processing ${sublist.size} entries.")
        val processor = LotusProcessRaw()
        val processed = processor.process(sublist)
        wdWriter.write(listOf(processed))
    }

    logger.info("Done!")
}
