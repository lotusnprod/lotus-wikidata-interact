/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.crossref

import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

/**
 * Interface for CrossRefAPIs, allows to mock
 */
@ExperimentalTime
interface CrossRefAPI : WebAPI {
    /**
     * The endpoint URL
     */
    val apiURL: String
}
