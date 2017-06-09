package ru.inforion.ttgen.utils

import java.util.*

object Utilities {
    val STATUS_ACTIVE: Short = 0
    val STATUS_OLD: Short = 1
    val STATUS_DELETED: Short = 2

    val AUTO_FWD_GEN = 180
    val AUTO_BWD_GEN = 180
    val SHIP_FWD_GEN = 365
    val SHIP_BWD_GEN = 365
    val RAIL_FWD_GEN = 60
    val RAIL_BWD_GEN = 60

    fun clearHMSMs(c : Calendar) {
        c.set(Calendar.HOUR, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.get(Calendar.DATE)
    }
}