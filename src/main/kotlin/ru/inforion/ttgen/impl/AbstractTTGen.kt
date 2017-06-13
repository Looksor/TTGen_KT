package ru.inforion.ttgen.impl

import org.apache.log4j.Logger
import ru.inforion.ttgen.entities.TimetableGenInfo
import ru.inforion.ttgen.utils.Utilities
import ru.inforion.ttgen.utils.Utilities.batch
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
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
            val list = getGenInfo()
            if (list.isEmpty()) return else startGenerate(list)
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
        query.setParameter("old", Utilities.STATUS_OLD)
        query.setParameter("active", Utilities.STATUS_ACTIVE)
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
        deleteQuery.setParameter("deleted", Utilities.STATUS_DELETED)
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

        createQuery.setParameter("activeStatus", Utilities.STATUS_ACTIVE)
        createQuery.setParameter("oldStatus", Utilities.STATUS_OLD)
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

    private fun getGenInfo() : List<TimetableGenInfo> {
        val genInfoList: List<TimetableGenInfo>
        val em : EntityManager = emf.createEntityManager()
        val selectQuery = em.createQuery("select x from TimetableGenInfo x " +
                "inner join $tableName y on x.ttid = y.id " +
                "where y.actualTo > x.lastGenerated and x.lastGenerated < :maxGen",
                TimetableGenInfo::class.java)
        selectQuery.setParameter("maxGen", lastgenMax)

        try {
            genInfoList = selectQuery.resultList
        } catch (e: Exception) {
            logError("Не удалось получить информацию для генерации расписаний")
            throw InnerTTGenException(e)
        }

        logInfo("Расписаний для обновления: " + genInfoList.size)
        return genInfoList
    }

    private fun startGenerate(atts : List<TimetableGenInfo>) {
        val threadCount = if (threadCount == -1) countThreads(atts.size) else threadCount
        logInfo("Размер пакета обновлений [$atts.size], установлено потоков [$threadCount]")
        val batchSize = atts.size / threadCount
        val split = atts.asSequence().batch(batchSize).toList()
        val executor = Executors.newFixedThreadPool(threadCount)
        split.forEach { g -> executor.execute { generateFactTT(g) } }
        executor.shutdown()
        try {
            logInfo("Ожидание завершения работы потоков")
        } catch (e : InterruptedException) {
            logError("Ошибка ожидания завершения работы потоков", e)
        }
    }

    private fun generateFactTT(genInfoList: List<TimetableGenInfo>) {


//        _em.clear()
//        var et = _em.getTransaction()
//        var countUpd = 0
//        var countAdded = 0
//        var lockErrors = 0
//
//        val updatedInfo = ArrayList()
//
//        var lockQueryInfo = _em.createNativeQuery("SELECT * FROM \"ATT_GEN_INFO\" " + "WHERE \"TTID\" = ?",
//                TimetableGenInfo::class.java)
//        var lockQueryTT = _em.createNativeQuery("SELECT * FROM \"TIMETABLE\" " + "WHERE \"TTID\" = ?",
//                getTimetableClass())
//
//        try {
//            et.begin()
//            for (ttgi in genInfoList) {
//                countUpd++
//
//                lockQueryInfo.setParameter(1, ttgi.getTtid())
//                lockQueryTT.setParameter(1, ttgi.getTtid())
//                try {
//                    ttgi = lockQueryInfo.getSingleResult() as TimetableGenInfo  // lock
//                } catch (e: Exception) {
//                    lockErrors++
//                    continue
//                }
//
//                var tt = lockQueryTT.getSingleResult() as ITimetable
//
//                var c = ttgi.getLastGenerated().clone()
//                if (c.before(_lastgenMin)) c = _lastgenMin.clone() as Calendar
//
//                var maxActual = tt.getActualTo()
//                if (maxActual.after(_lastgenMax)) maxActual = _lastgenMax.clone() as Calendar
//
//                val departTime = tt.getDepartTime()
//
//                // for first day (skip if depart < actualFrom)
//                if (c.before(maxActual) && (c.get(Calendar.HOUR_OF_DAY) > departTime.get(Calendar.HOUR_OF_DAY) || c.get(Calendar.HOUR_OF_DAY) == departTime.get(Calendar.HOUR_OF_DAY) && c.get(Calendar.MINUTE) > departTime.get(Calendar.MINUTE))) {
//                    c.add(Calendar.DATE, 1)
//                    c.set(Calendar.HOUR_OF_DAY, 0)
//                    c.set(Calendar.MINUTE, 0)
//                    c.get(Calendar.DATE)
//                }
//
//                while (c.before(maxActual)) {
//
//                    if (tt.getDmYear() !== c.get(Calendar.YEAR)) {
//                        regenDaymask(tt, c.get(Calendar.YEAR))
//                        tt = _em.merge(tt)
//                    }
//
//                    var createFact = false
//                    // last day
//                    if (c.get(Calendar.MONTH) == maxActual.get(Calendar.MONTH) && c.get(Calendar.DATE) == maxActual.get(Calendar.DATE)) {
//                        if (maxActual.get(Calendar.HOUR_OF_DAY) > departTime.get(Calendar.HOUR_OF_DAY) || maxActual.get(Calendar.HOUR_OF_DAY) == departTime.get(Calendar.HOUR_OF_DAY) && maxActual.get(Calendar.MINUTE) > departTime.get(Calendar.MINUTE)) {
//                            createFact = true  // create for last day
//                        }
//                    } else
//                        createFact = true   // create for common day
//
//                    if (createFact && tt.getDaymask().charAt(Calendars.getDOY(c)) === '1') {
//                        _em.persist(getATS(departTime, c, tt))
//                        countAdded++
//                    }
//
//                    c.add(Calendar.DATE, 1)
//                }
//
//                if (c.after(maxActual)) {
//                    c = maxActual
//                    c.get(Calendar.DATE)
//                }
//
//                ttgi.setLastGenerated(c)
//                updatedInfo.add(ttgi)
//                _em.merge(ttgi)
//                _em.flush()
//
//                if (updatedInfo.size >= 200) {
//                    et.commit()
//                    val percent = countUpd.toDouble() / genInfoList.size.toDouble() * 100
//                    logInfo("Промежуточный коммит. Обновлено информации: (" + String.format("%.1f", percent) + "%)" +
//                            " {Upd : " + countUpd + " Add : " + countAdded + " lockErr : " + lockErrors + " }")
//
//                    updatedInfo.clear()
//                    _em.clear()
//                    _em = _emf.createEntityManager()
//                    lockQueryInfo = _em.createNativeQuery(
//                            "SELECT * FROM \"ATT_GEN_INFO\" WHERE \"TTID\" = ?", TimetableGenInfo::class.java)
//                    lockQueryTT = _em.createNativeQuery(
//                            "SELECT * FROM \"TIMETABLE\" WHERE \"TTID\" = ?", getTimetableClass())
//                    et = _em.getTransaction()
//                    et.begin()
//                }
//            }
//            et.commit()
//            _em.clear()
//
//            logInfo("Завершение обновления. Обновлено информации: (100.0%)")
//            logInfo("Обновлено расписаний: " + countUpd)
//            logInfo("Ошибок получения блокировки:" + lockErrors)
//            logInfo("Добавлено фактических расписаний: " + countAdded)
//        } catch (e: Exception) {
//            logError("Сбой работы с БД в процессе обновления")
//            try {
//                et.rollback()
//            } catch (ex: Exception) {
//                logError(_segment + "Ошибка отката транзакции")
//            }
//
//            e.printStackTrace()
//            throw GeneratorException(e)
//        }

    }

//    private fun getATS(depart: Calendar, current: Calendar, tt: ITimetable): IActualTimetableShort {
//        val departActual = depart.clone() as Calendar
//
//        departActual.set(Calendar.YEAR, current.get(Calendar.YEAR))
//        departActual.set(Calendar.MONTH, current.get(Calendar.MONTH))
//        departActual.set(Calendar.DATE, current.get(Calendar.DATE))
//        departActual.get(Calendar.MILLISECOND)
//
//        val ats = tt.getATS()
//        ats.setTtid(tt)
//        ats.setRouteDate(departActual)
//
//        return ats
//    }

    private fun countThreads(i: Int): Int {
        if (i < 50) return 1
        if (i < 100) return 2
        if (i < 400) return 3
        if (i < 1000) return 4
        return 5
    }

    fun logInfo(str : String) : Unit = logger.info("[$segment] $str")
    fun logError(str : String) : Unit = logger.error("[$segment] $str")
    fun logError(str : String, e : Exception) : Unit = logger.error("[$segment] $str", e)

    abstract fun regenDM(timetable: Any, year: Int)

    abstract val threadCount : Int
    abstract val lastgenMin: Calendar
    abstract val lastgenMax: Calendar
    abstract val segment : String
    abstract val tableName : String
    abstract val emf : EntityManagerFactory
    abstract val ttClass : Class<*>
}

private class InnerTTGenException(override val cause : Exception) : Exception()
