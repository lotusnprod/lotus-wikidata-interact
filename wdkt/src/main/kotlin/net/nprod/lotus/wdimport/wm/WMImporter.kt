/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

@file:Suppress("unused")

package net.nprod.lotus.wdimport.wm

/*

/**
 * This is for later when we will import wikimedia
 */

import net.nprod.lotus.wdimport.wd.publishing.EnvironmentVariableError
import org.wikipedia.WMFWiki
import org.wikipedia.Wiki

import java.io.IOException

import javax.security.auth.login.FailedLoginException

class WMConnector {
    private val user: String = System.getenv("WIKIDATA_USER")
        ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_USER")
    private val password: String = System.getenv("WIKIDATA_PASSWORD")
        ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_PASSWORD")
    private val wiki = WMFWiki.newSessionFromDBName("wikidatawiki")

    init {
        wiki.throttle = 5000
        val owner = System.getenv("OWNER")
            ?: throw EnvironmentVariableError("Missing environment variable OWNER (please read the doc!)")
        wiki.userAgent = "LOTUS Importer (owned by $owner)/0.1"
        wiki.assertionMode = Wiki.ASSERT_BOT
    }

    fun connect() {
        try {
            wiki.login(user, password)
        } catch (ex: FailedLoginException) {
            // deal with failed login attempt
            ex.printStackTrace()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}
*/
