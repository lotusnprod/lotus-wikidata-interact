/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.services

import net.nprod.lotus.wikidata.upload.models.Log
import org.springframework.stereotype.Service

@Service
class LogService {
    private var _currentLog: Log = Log(0, 0, 0, 0, listOf())

    val currentLog: Log
        get() = _currentLog
}