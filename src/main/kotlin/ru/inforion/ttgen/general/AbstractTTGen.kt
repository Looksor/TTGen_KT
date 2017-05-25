package ru.inforion.ttgen.general

import org.apache.log4j.Logger
import java.lang.Exception
import java.util.*
import javax.persistence.EntityManagerFactory
abstract class AbstractTTGen {
    abstract val logger : Logger

    fun run(): Unit {
        try {
            logInfo("Начало работы генератора расписаний")
            // TODO FF
        } catch (e : InnerTTGenException) {
            logError("Ошибка работы генератора, ", e.cause)
        }
    }



    fun logInfo(str : String) : Unit = logger.info("[$segment] $str")
    fun logError(str : String) : Unit = logger.error("[$segment] $str")
    fun logError(str : String, e : Exception) : Unit = logger.error("[$segment] $str", e)

    abstract fun regenDM(timetable: Any, year: Int)

    abstract val lastgenMin: Calendar
    abstract val lastgenMax: Calendar
    abstract val segment : String
    abstract val tableName : String
    abstract val emf : EntityManagerFactory
    abstract val ttClass : Class<*>
}

private class InnerTTGenException(override val cause : Exception) : Exception()
