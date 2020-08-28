package net.nprod.onpdb.wdimport.wm

import net.nprod.onpdb.wdimport.wd.EnvironmentVariableError
import org.wikipedia.WMFWiki
import org.wikipedia.WMFWiki.newSessionFromDBName
import org.wikipedia.Wiki

import java.io.IOException

import javax.security.auth.login.FailedLoginException


class WMConnector() {
    private val user: String = System.getenv("WIKIDATA_USER")
        ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_USER")
    private val password: String = System.getenv("WIKIDATA_PASSWORD")
        ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_PASSWORD")
    private val wiki = WMFWiki.newSessionFromDBName("wikidatawiki")

    init {
        wiki.throttle = 5000
        wiki.userAgent = "My Bot/1.0"
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


fun main() {
    val wmConnector = WMConnector()
    wmConnector.connect()
}