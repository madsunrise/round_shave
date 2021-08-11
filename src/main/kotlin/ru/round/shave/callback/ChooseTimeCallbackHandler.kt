package ru.round.shave.callback

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ChooseTimeCallbackHandler : BaseCallbackHandler<LocalTime>(Prefixes.CHOOSE_TIME_PREFIX) {

    private val FORMATTER = DateTimeFormatter.ofPattern("HH.mm")

    override fun encodeData(data: LocalTime): String {
        return FORMATTER.format(data)
    }

    override fun decodeData(value: String): LocalTime {
        return LocalTime.parse(value, FORMATTER)
    }
}