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

    fun close() {
        // Not sure we need to close all of them
        bufferedReader.close()
        inputStreamReader.close()
        inputStream.close()
        fileInputStream.close()
    }
}
