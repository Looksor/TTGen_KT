package ru.inforion.ttgen.impl

import org.apache.log4j.Logger
import ru.inforion.egis.commons.data.validator.impl.ValidationConstants
import ru.inforion.egis.commons.utils.Calendars
import ru.inforion.ttgen.entities.auto.Timetable
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

class AutoTTGen(override val logger: Logger, props: Map<String, String?>) : AbstractTTGen() {
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
        val fwdInterval: Int = ValidationConstants.AUTO_ACTUAL_FWD_INTERVAL
        val bwdInterval: Int = ValidationConstants.AUTO_ACTUAL_BWD_INTERVAL
        lastgenMin = Calendar.getInstance()
        lastgenMax = Calendar.getInstance()
        lastgenMin.add(Calendar.DATE, if (bwdInterval < 0) bwdInterval else -bwdInterval)
        lastgenMax.add(Calendar.DATE, if (fwdInterval < 0) -fwdInterval else fwdInterval)
        Calendars.clearHMSMss(lastgenMin)
        Calendars.clearHMSMss(lastgenMax)
        emf = Persistence.createEntityManagerFactory("AUTO_TIMETABLE_V2_PU_TTGEN", props)
    }
}