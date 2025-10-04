/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import net.nprod.lotus.helpers.GZIPReader
import java.io.BufferedReader
import java.io.File

/**
 * Attempts to open a file as GZIP, falling back to plain text if not compressed.
 *
 * @param fileName Path to the file to open.
 * @return BufferedReader for the file contents.
 */
fun tryGzipThenNormal(fileName: String): BufferedReader =
    try {
        GZIPReader(fileName).bufferedReader
    } catch (e: java.util.zip.ZipException) {
        e.message
        // If not a GZIP file, fall back to normal reader
        File(fileName).bufferedReader()
    }

/**
 * Reader for TSV/CSV files using Univocity, with support for skipping and limiting records.
 *
 * @param T The type of object to produce from each record.
 * @property f Function to convert a Univocity record to type T.
 */
class UnivocityBasedReader<T>(
    private val f: (com.univocity.parsers.common.record.Record) -> T,
) {
    private var csvParser: CsvParser? = null

    /**
     * Number of records to skip.
     */
    var skip: Int? = null

    /**
     * Maximal number of records to read.
     */
    var maximalNumber: Long? = null
    private var bufferedReader: BufferedReader? = null

    /**
     * Opens the specified file for reading, configuring the parser.
     *
     * @param fileName Path to the file to open.
     */
    fun open(fileName: String) {
        val settingsParser = CsvParserSettings()
        settingsParser.format.setLineSeparator("\n")
        settingsParser.format.setDelimiter("\t")
        settingsParser.isAutoClosingEnabled = false
        maximalNumber?.let { settingsParser.numberOfRecordsToRead = it + (skip ?: 0) }
        settingsParser.isHeaderExtractionEnabled = true

        // Try to open as GZIP, fallback to plain text
        bufferedReader = tryGzipThenNormal(fileName)
        csvParser =
            bufferedReader?.let { reader ->
                CsvParser(settingsParser).also { parser ->
                    parser.beginParsing(reader)
                    skip?.let { repeat(it) { parser.parseNextRecord() } }
                }
            }
    }

    /**
     * Closes the parser and the underlying reader.
     */
    fun close() {
        csvParser?.stopParsing()
        bufferedReader?.close()
    }

    /**
     * Reads records from the file and converts them to type T.
     *
     * @return List of converted records.
     */
    fun read(): List<T> {
        val list = mutableListOf<T>()
        while (csvParser?.context?.isStopped == false) { // we have a false for null matching
            val record: com.univocity.parsers.common.record.Record = csvParser?.parseNextRecord() ?: break
            list.add(f(record))
        }
        return list
    }
}
