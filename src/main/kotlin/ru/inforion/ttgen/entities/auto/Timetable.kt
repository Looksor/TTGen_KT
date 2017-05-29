package ru.inforion.ttgen.entities.auto

import javax.persistence.*


@Entity(name = "AutoTimetable")
@Table(name = "\"TIMETABLE\"")
class Timetable {
    @Id
    @Column(name = "\"TTID\"")
    var id: Long? = null

    @Column(name = "\"NAME\"")
    var name: String? = null

    @Column(name = "\"REGISTERED_NAME\"")
    var regName: String? = null

    @Column(name = "\"DAYMASK\"")
    var daymask: String? = null
    
    @Column(name = "\"DMYEAR\"")
    private var dmYear = -1
    
    @Column(name = "\"ORIG_DAYMASK\"")
    private var origDaymask: String? = null
    
    @Column(name = "\"OPERATOR_ID\"")
    private var operatorId: Long? = null
    
    @Column(name = "\"ROUTE_START\"")
    private var routeStart: Long? = null
    
    @Column(name = "\"ROUTE_END\"")
    private var routeEnd: Long? = null
    
    @Column(name = "\"SUBJECT\"")
    private var tsAffinity: Long? = null
    
    @Column(name = "\"INTNL\"")
    private var international: Short? = null
    
    @Column(name = "\"ISAUTOGEN\"")
    private var isAutoGen: Short? = null
    
    @Column(name = "\"CHARTERED\"")
    private var chartered: Short? = null
    
    @Column(name = "\"ACTUAL_FROM\"")
    @Temporal(TemporalType.TIMESTAMP)
    private var actualFrom: java.util.Calendar? = null
    
    @Column(name = "\"ACTUAL_TO\"")
    @Temporal(TemporalType.TIMESTAMP)
    private var actualTo: java.util.Calendar? = null
    
    @Column(name = "\"FOREIGN_ID\"")
    private var foreignId: String? = null

    @Column(name = "\"ISGID\"")
    private var isgid: Long? = null
    

    @Column(name = "\"ISUID\"")
    private var isuid: Long? = null
    
    @Column(name = "\"STATUS\"")
    private var status: Short? = java.lang.Short.valueOf(0)
    
    @OneToMany(mappedBy = "ttid")
    private var routePoints: List<RoutePoint> = java.util.ArrayList()

    @Transient
    private var source: ru.egis_otb.data.timetable.Timetable? = null

}