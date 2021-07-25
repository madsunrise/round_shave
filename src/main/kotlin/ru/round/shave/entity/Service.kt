package ru.round.shave.entity

enum class Service(val durationInMinutes: Int, val serviceName: String, val price: Int) {
    MEN_HAIRCUT(60, "Мужская стрижка", 1000),
    BUZZ_HAIRCUT(30, "Стрижка машинкой", 500),
    CHILDREN_HAIRCUT(60, "Детская стрижка (до 12 лет)", 800),
    HAIR_PARTING(15, "Пробор / Окантовка", 200),
    HAIR_STYLING(15, "Укладка", 600),
    BEARD_TRIM_RAZOR(45, "Моделирование бороды", 900),
    BEARD_TRIM(30, "Стрижка бороды и усов", 700),
    HEAD_SHAVE(60, "Бритье головы", 900);

    fun getDisplayName(): String {
        return "$serviceName - $price\u20BD"
    }
}