package ru.round.shave.callback

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ChooseDateCallbackHandler : BaseCallbackHandler<LocalDate>() {

    override val prefix: String = Prefixes.CHOOSE_DATE_PREFIX

    private val FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun encodeData(data: LocalDate): String {
        return FORMATTER.format(data)
    }

    override fun decodeData(value: String): LocalDate {
        return LocalDate.parse(value, FORMATTER)
    }
}