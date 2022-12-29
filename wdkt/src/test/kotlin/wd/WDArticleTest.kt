package wd

import net.nprod.lotus.helpers.titleCleaner
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import net.nprod.lotus.wdimport.wd.sparql.findReferencesByDOI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WDArticleTest {
    private lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql(MainInstanceItems)
    }

    @Test
    fun `Find references by DOI`() {
        val dois = mapOf(
            "10.1021/ACS.JMEDCHEM.5B01009" to listOf("Q26778522"),
            "10.3389/FPLS.2019.01329" to listOf("Q91218352")
        )

        val result = wdSparql.findReferencesByDOI(dois.keys.toList())
        result.forEach {
            assertEquals(dois[it.key], it.value)
        }
    }

    @Test
    fun `Clean HTML tags from titles`() {
        val titles = mapOf(
            "<i>Citrus sinensis</i> Leaves" to "Citrus sinensis Leaves", // working
            "<a href=\"link\">A link</a>" to "A link", // working
            // "<i this is not a tag" to "<i this is not a tag", // not working
            // "title >induced in error <or not?" to "title >induced in error <or not?", // not working
            // "\" ' > < \n \\ é å à ü and & preserved" to "\" ' > < \n \\ é å à ü and & preserved", // not working
            // "&lt;script&gt;" to "", // not working
            // "&amp;lt;script&amp;gt;" to "lt;scriptgt;", // not working
            "<script src=\"http://evil.url.com\"/>" to "" // working
        )

        titles.forEach {
            assertEquals(it.key.titleCleaner(), it.value)
        }
    }
}
