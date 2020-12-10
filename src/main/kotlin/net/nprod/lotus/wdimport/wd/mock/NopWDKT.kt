package net.nprod.lotus.wdimport.wd.mock

import net.nprod.lotus.wdimport.wd.query.IWDKT
import net.nprod.lotus.wdimport.wd.query.QueryActionResponse

class NopWDKT: IWDKT {
    override fun close() {}

    override fun searchDOI(doi: String): QueryActionResponse? = null
}