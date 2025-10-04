/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.gbif

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
interface GBIFAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String
}
