package ru.round.shave.entity

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "appointment")
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "appointment_service",
        joinColumns = [JoinColumn(name = "appointment_id")],
        inverseJoinColumns = [JoinColumn(name = "service_id")]
    )
    val services: List<Service>,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime
) {
    constructor() : this(
        user = User(),
        services = emptyList(),
        startTime = LocalDateTime.MIN,
        endTime = LocalDateTime.MIN
    )
}