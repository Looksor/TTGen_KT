package ru.inforion.ttgen.impl

import org.apache.log4j.Logger
import ru.inforion.egis.datatypes.helpers.RecordStatusConverter
import java.lang.Exception
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.TemporalType

abstract class AbstractTTGen {
    abstract val logger : Logger

    fun run(): Unit {
        try {
            logInfo("Начало работы генератора расписаний")
            updateStatus()
            deleteGenInfoInactive()
            addGenInfo()
            // TODO FF
        } catch (e : InnerTTGenException) {
            logError("Ошибка работы генератора, ", e.cause)
        }
    }

    private fun updateStatus() {
        val today = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"))
        val em : EntityManager = emf.createEntityManager()
        val et = em.transaction
        val query = em.createQuery("update $tableName x set x.status = :old " +
                "where x.status = :active and x.actualTo < :today")
        query.setParameter("old", RecordStatusConverter.OLD)
        query.setParameter("active", RecordStatusConverter.ACTIVE)
        query.setParameter("today", today)

        try {
            et.begin()
            val del = query.executeUpdate()
            et.commit()
            logInfo("Обновлено статусов устаревших расписаний: " + del)
        } catch (e: Exception) {
            logError("Не удалось обновить информацию об устаревших расписаниях")
            throw InnerTTGenException(e)
        }

    }

    private fun deleteGenInfoInactive() {
        val em : EntityManager = emf.createEntityManager()
        val et = em.transaction
        val deleteQuery = em.createQuery("delete from TimetableGenInfo x " +
                "where x.ttid in (select y.id from $tableName y " +
                "where y.status = :deleted or DATE(y.actualTo) < DATE(:minGen))")
        deleteQuery.setParameter("deleted", RecordStatusConverter.DELETED)
        deleteQuery.setParameter("minGen", lastgenMin, TemporalType.TIMESTAMP)

        try {
            et.begin()
            val rst = deleteQuery.executeUpdate()
            logInfo("Удалено устаревшей информации о генерации: " + rst)
            et.commit()
        } catch (e: Exception) {
            logError("Не удалось удалить информацию генератора для неактивных расписаний")
            try {
                et.rollback()
            } catch (ex: Exception) {
                logError("Ошибка отката транзакции")
            }
            throw InnerTTGenException(e)
        }
    }

    private fun addGenInfo() {
        val em : EntityManager = emf.createEntityManager()
        val et = em.transaction
        val createQuery = em.createQuery("insert into TimetableGenInfo (ttid, lastGenerated) " +
                "select x.id, x.actualFrom from $tableName x where " +
                "(x.status = :activeStatus or (x.status = :oldStatus and x.actualTo > :minGen)) " +
                "and not (x.id in (select y.ttid from TimetableGenInfo y))")

        createQuery.setParameter("activeStatus", RecordStatusConverter.ACTIVE)
        createQuery.setParameter("oldStatus", RecordStatusConverter.OLD)
        createQuery.setParameter("minGen", lastgenMin)

        try {
            et.begin()
            val i = createQuery.executeUpdate()
            logInfo("Добавлена информация о генерации для расписаний: " + i)
            et.commit()
        } catch (e: Exception) {
            logError("Не удалось добавить информацию для генерации расписаний")
            try {
                et.rollback()
            } catch (ex: Exception) {
                logError("Ошибка отката транзакции")
            }
            throw InnerTTGenException(e)
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
