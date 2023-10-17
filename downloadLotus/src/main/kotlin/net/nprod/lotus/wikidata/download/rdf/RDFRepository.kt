/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wikidata.download.rdf

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.nativerdf.NativeStore
import org.slf4j.LoggerFactory
import java.io.File

/**
 * A local RDFRepository to store all the acquired SPARQL data
 *
 * We use that to make sure that we can access the repo. We may add
 * the querying directly here as well if needed.
 */
class RDFRepository(location: File) {
    val repository: SailRepository
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        location.mkdirs()
        val file =
            location.also {
                if (!it.canWrite() || !it.canRead()) throw AccessDeniedException(it)
            }
        logger.info("Opening the NativeStore at $file")
        repository = SailRepository(NativeStore(file))
    }
}
