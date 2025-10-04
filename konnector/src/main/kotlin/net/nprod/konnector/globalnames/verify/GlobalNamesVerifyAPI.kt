/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021-2022 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface GlobalNamesVerifyAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String
}
