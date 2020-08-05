package wd

import net.nprod.onpdb.helpers.GZIPRead
import net.nprod.onpdb.helpers.parseTSVFile
import net.nprod.onpdb.wdimport.wd.MainInstanceItems
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.BeforeEach
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import net.nprod.onpdb.wdimport.wd.sparql.findCompoundsByInChIKey

class WDCompoundSearchPerformanceTest {
    lateinit var wdSparql: WDSparql
    val logger = LogManager.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql(MainInstanceItems)
    }

    // We don't want that to run everytime we run the basic tests
    //@Test
    fun findCompoundsByInChIKeyChunked() {
        val file = GZIPRead("/home/bjo/Store/01_Research/opennaturalproductsdb/data/external/dbSource/napralert/napralert_matched_final_unified.tsv.gz")
        val inchies = parseTSVFile(file)?.map {
            it.getString("InChIKey")
        }?: listOf()

        logger.info("Found ${inchies.size} Inchies")
        val chunks = 10000
        val result = wdSparql.findCompoundsByInChIKey(inchies, chunkSize = chunks) {
            logger.info("Processed $chunks")
        }
        logger.info("Found ${result.size} Inchies already in wikidata, that's ${100*result.size/inchies.size}% of known inchies.")
    }
}