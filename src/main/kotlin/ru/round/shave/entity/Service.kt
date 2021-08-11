package ru.round.shave.entity

import javax.persistence.*

@Entity
@Table(name = "service")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @Column(name = "name", nullable = false, unique = true)
    val name: String,

    @Column(name = "price", nullable = false)
    val price: Int,

    @Column(name = "duration", nullable = false)
    val duration: Int
) {
    constructor() : this(
        name = "",
        price = 0,
        duration = 0
    )

    fun getDisplayName(): String {
        return name
    }

    fun getDisplayNameWithPrice(): String {
        return "$name: $price\u20BD"
    }

    fun getDisplayPrice(): String {
        return "$price\u20BD"
    }

    override fun toString(): String {
        return "Service(id=$id, name='$name', price=$price, duration=$duration)"
    }
}