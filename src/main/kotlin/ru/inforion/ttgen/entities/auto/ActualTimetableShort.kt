package ru.inforion.ttgen.entities.auto

import javax.persistence.*

@Entity(name = "ActualTimetableShort")
@Table(name = "\"ACTUAL_TIMETABLE_SHORT\"")
class ActualTimetableShort {
    @Id
    @Column(name = "\"ATID\"")
    var atid : Long? = null

    @Column(name = "\"RTDATE\""    )
    @Temporal(TemporalType.TIMESTAMP)
    var routeDate : java.util.Calendar? = null

    @ManyToOne
    var ttid : Timetable? = null
}
