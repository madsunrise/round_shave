package ru.round.shave.entity

enum class Service(val durationInMinutes: Int, val userVisibleName: String) {
    HAIRCUT(60, "Стрижка"),
    SHAVING(30, "Бритье"),
}