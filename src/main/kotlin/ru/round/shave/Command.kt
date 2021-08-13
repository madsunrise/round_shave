package ru.round.shave

enum class Command(val key: String) {
    START("start"),
    NEW_APPOINTMENT("new"),
    PRICE_LIST("prices"),
    MY_APPOINTMENTS("appointments"),
    MASTER_CONTACTS("contacts");

    companion object {
        fun findCommand(textCommand: String): Command? {
            val withoutSlash = textCommand.dropWhile { it == '/' }
            return values().find { it.key.equals(withoutSlash, ignoreCase = true) }
        }
    }
}
