/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.helpers

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * Utility class for reading GZIP-compressed files as text.
 *
 * @property bufferedReader BufferedReader for reading the decompressed file.
 *
 * @constructor Opens the specified GZIP file for reading.
 * @param name Path to the GZIP file.
 */
class GZIPReader(
    name: String,
) {
    val bufferedReader: BufferedReader
    private val fileInputStream: FileInputStream = FileInputStream(name)
    private val inputStreamReader: InputStreamReader
    private val inputStream: GZIPInputStream = GZIPInputStream(fileInputStream)

    init {
        inputStreamReader = InputStreamReader(inputStream)
        bufferedReader = BufferedReader(inputStreamReader)
    }

    /**
     * Closes all underlying streams and the buffered reader.
     *
     * It is important to close all resources to avoid file descriptor leaks.
     */
    fun close() {
        // Not sure we need to close all of them
        bufferedReader.close()
        inputStreamReader.close()
        inputStream.close()
        fileInputStream.close()
    }
}
