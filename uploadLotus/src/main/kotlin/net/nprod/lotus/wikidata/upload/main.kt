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

// import org.springframework.boot.runApplication

// @Suppress("SpreadOperator")
@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("MainThread")
    // runApplication<LotusImporter>(*args)
    logger.info("Hello!")
    val tsvReader = UnivocityBasedReader<LotusRaw> {
        LotusRaw.fromRecord(it)
    }

    val filePrepath = "../"
    val fileName = args[1]
    val filePath = filePrepath + fileName
    val processor = LotusProcessRaw()
    val wdWriter = WikiDataWriter()

    // tsvReader.maximalNumber = 1200
    tsvReader.skip = 0
    tsvReader.open(filePath)
    val list = tsvReader.read()
    logger.info("Processing ${list.size} entries.")
    val processed = processor.process(list)

    wdWriter.write(listOf(processed))

    logger.info("Done!")

    tsvReader.close()
}
