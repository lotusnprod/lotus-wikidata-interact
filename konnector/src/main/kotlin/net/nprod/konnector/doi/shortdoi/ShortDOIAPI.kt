/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.doi.shortdoi

import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface ShortDOIAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String
}
