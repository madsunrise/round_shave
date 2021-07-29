package ru.round.shave.entity

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table(name = "state")
data class State(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_id", nullable = true)
    val service: Service? = null,

    @Column(name = "day", nullable = true)
    val day: LocalDate? = null,

    @Column(name = "time", nullable = true)
    val time: LocalTime? = null,

    @Column(name = "step", nullable = false)
    val currentStep: Step
) {

    constructor() : this(user = User(), currentStep = Step.INITIAL)

    enum class Step {
        INITIAL,
        SERVICE_CHOSEN,
        DAY_CHOSEN,
        TIME_CHOSEN,
        CONFIRMED
    }
}