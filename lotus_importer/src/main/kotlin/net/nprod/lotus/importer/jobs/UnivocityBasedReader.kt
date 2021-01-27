/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import com.univocity.parsers.tsv.TsvParser
import net.nprod.lotus.helpers.GZIPReader
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.core.io.Resource
import java.io.BufferedReader
import java.io.File


fun tryGzipThenNormal(fileName: String): BufferedReader = try {
    GZIPReader(fileName).bufferedReader
} catch (e: java.util.zip.ZipException) {
    File(fileName).bufferedReader()
}

class UnivocityBasedReader<T>(private val f: (com.univocity.parsers.common.record.Record) -> T) :
    ResourceAwareItemReaderItemStream<List<T>>, ItemStreamReader<List<T>> {
    private var csvParser: CsvParser? = null
    private var skip: Int? = null
    private var maxItemCount: Long? = null
    private var bufferedReader: BufferedReader? = null

    override fun open(executionContext: ExecutionContext) {
        val settingsParser = CsvParserSettings()
        settingsParser.format.setLineSeparator("\n")
        settingsParser.format.setDelimiter("\t")
        settingsParser.isAutoClosingEnabled = false
        maxItemCount?.let { settingsParser.numberOfRecordsToRead = it + (skip ?: 0) }
        settingsParser.isHeaderExtractionEnabled = true

        bufferedReader = tryGzipThenNormal("data/sorted_platinum.tsv")
        csvParser = bufferedReader?.let { reader ->
            CsvParser(settingsParser).also { parser ->
                parser.beginParsing(reader)
                skip?.let { repeat(it) { parser.parseNextRecord() } }
            }
        }
    }

    override fun update(executionContext: ExecutionContext) {
        //TODO("Not yet implemented update $executionContext")
    }

    override fun close() {
        csvParser?.stopParsing()
        bufferedReader?.close()
    }

    override fun read(): List<T>? {
        val list = mutableListOf<T>()
        if (csvParser?.context?.isStopped == true) return null
        while (true) {
            if (csvParser?.context?.isStopped == true) return list
            val record: com.univocity.parsers.common.record.Record = csvParser?.parseNextRecord() ?: return list
            list.add(f(record))
        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        stepExecution.jobParameters.getLong("max_records")?.let { maxItemCount = it }
        stepExecution.jobParameters.getLong("skip")?.let { skip = it.toInt() }
    }

    override fun setResource(resource: Resource) {
        TODO("Not yet implemented resource $resource")
    }
}
