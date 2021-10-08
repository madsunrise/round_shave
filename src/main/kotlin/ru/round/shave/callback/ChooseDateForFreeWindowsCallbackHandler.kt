package ru.round.shave.callback

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ChooseDateForFreeWindowsCallbackHandler :
    BaseCallbackHandler<LocalDate>(Prefixes.CHOOSE_DATE_FREE_WINDOWS_FOR_ADMIN_PREFIX) {

    private val FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun encodeData(data: LocalDate): String {
        return FORMATTER.format(data)
    }

    override fun decodeData(value: String): LocalDate {
        return LocalDate.parse(value, FORMATTER)
    }
}