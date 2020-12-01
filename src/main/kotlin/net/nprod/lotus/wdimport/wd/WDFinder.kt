package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql


class WDFinder(val wdkt: WDKT, val sparql: ISparql) {
    fun close() {
        wdkt.close()
    }
}