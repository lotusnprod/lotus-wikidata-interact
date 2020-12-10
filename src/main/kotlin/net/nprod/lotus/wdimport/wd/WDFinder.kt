package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.query.IWDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql

class WDFinder(val wdkt: IWDKT, val sparql: ISparql) {
    fun close() {
        wdkt.close()
    }
}