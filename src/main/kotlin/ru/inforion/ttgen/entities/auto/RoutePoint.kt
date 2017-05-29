package ru.inforion.ttgen.entities.auto

import java.util.*
import javax.persistence.*

@Entity(name = "RoutePoint")
@Table(name = "\"AUTO_ROUTE_POINT\"")
class RoutePoint {
    @Id
    @Column(name = "\"ARPID\"")
    private var id: Long? = null

    @ManyToOne
    private var ttid: Timetable? = null

    @Column(name = "\"ARRIVE_TIME\"")
    private var arriveTime: Calendar? = null

    @Column(name = "\"DEPART_TIME\"")
    private var departTime: Calendar? = null

    @Column(name = "\"PATH_INDEX\"")
    private var pathIndex: Short? = null

    @Column(name = "\"START_DISTANCE\"")
    private var startDistance: Long? = null

    @Column(name = "\"STOP_TIME_INTV\"")
    private var stopTimeInterval: Long? = null

    @Column(name = "\"STATION\"")
    private var station: Long? = null

    @Column(name = "\"TIME_FROM_START\"")
    private var timeFromStart: Long? = null

    @Column(name = "\"ARRIVE_LCL\"")
    @Temporal(TemporalType.TIMESTAMP)
    private var arriveTimeLocal: Calendar? = null

    @Column(name = "\"DEPART_LCL\"")
    @Temporal(TemporalType.TIMESTAMP)
    private var departTimeLocal: Calendar? = null
}