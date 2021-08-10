package ru.round.shave.entity

import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table(name = "working_hours")
data class WorkingHours(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @Column(name = "day", nullable = false, unique = true)
    val day: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime
) {
    constructor() : this(
        day = LocalDate.MIN,
        startTime = LocalTime.MIN,
        endTime = LocalTime.MIN
    )

    override fun toString(): String {
        return "WorkingHours(id=$id, day=$day, startTime=$startTime, endTime=$endTime)"
    }
}