package ru.inforion.ttgen.impl

import org.apache.log4j.Logger
import ru.inforion.ttgen.entities.auto.Timetable
import ru.inforion.ttgen.utils.Utilities
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

class AutoTTGen(override val logger: Logger,
                override val threadCount: Int,
                props: Map<String, String?>) : AbstractTTGen() {
    override fun regenDM(timetable: Any, year: Int) {
        val tt = timetable as Timetable
//        val yc = CalendarConverter.abstractToYear(CalendarConverter.getCalendar(tt.origDaymask), year)
//        tt.daymask = yc.daymask
//        tt.dmYear = yc.dmyear!!
    }

    override val ttClass: Class<*>
        get() = Timetable::class.java

    override val emf: EntityManagerFactory

    override val segment: String
        get() = "Auto"

    override val tableName: String
        get() = "AutoTimetable"

    override val lastgenMin: Calendar

    override val lastgenMax: Calendar

    init {
        val fwdInterval: Int = Utilities.AUTO_FWD_GEN
        val bwdInterval: Int = Utilities.AUTO_BWD_GEN
        lastgenMin = Calendar.getInstance()
        lastgenMax = Calendar.getInstance()
        lastgenMin.add(Calendar.DATE, if (bwdInterval < 0) bwdInterval else -bwdInterval)
        lastgenMax.add(Calendar.DATE, if (fwdInterval < 0) -fwdInterval else fwdInterval)
        Utilities.clearHMSMs(lastgenMin)
        Utilities.clearHMSMs(lastgenMax)
        emf = Persistence.createEntityManagerFactory("AUTO_TIMETABLE_V2_PU_TTGEN", props)
    }
}