package ru.inforion.ttgen.entities

import java.util.*
import javax.persistence.*

@Entity(name = "TimetableGenInfo")
@Table(name = "\"ATT_GEN_INFO\"")
class TimetableGenInfo {
    @Id
    @Column(name = "\"TTID\"")
    private var ttid: Long? = null

    @Column(name = "\"LAST_GEN\"")
    @Temporal(TemporalType.TIMESTAMP)
    private var lastGenerated: Calendar? = null

}