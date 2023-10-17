/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.rdf

import org.apache.logging.log4j.LogManager
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFHandler
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.sail.memory.MemoryStore
import java.io.File
import java.io.FileNotFoundException

/**
 * Create a local SPARQL repository potentially persistent
 */
class RepositoryManager(persistent: Boolean, persistenceLocation: String) {
    private val logger = LogManager.getLogger(RepositoryManager::class.qualifiedName)

    /**
     * This is the SailRepository itself that can be accessed
     */
    val repository: SailRepository = SailRepository(MemoryStore())

    init {
        if (persistent) {
            try {
                logger.info("Loading old data")
                val file =
                    File(persistenceLocation).also {
                        if (!it.canWrite() || !it.canRead()) throw AccessDeniedException(it)
                    }
                repository.let {
                    it.connection
                    it.connection.add(file.inputStream(), "", RDFFormat.RDFXML)
                }
            } catch (e: FileNotFoundException) {
                logger.info("There is no data from a previous test run.")
            }
        }
    }

    fun write(filename: String) {
        val file = File(filename).also { if (!it.canWrite()) throw AccessDeniedException(it) }.bufferedWriter()
        repository.let {
            it.connection
            val writer: RDFHandler = Rio.createWriter(RDFFormat.RDFXML, file)
            it.connection.export(writer)
        }
        file.close()
    }
}
