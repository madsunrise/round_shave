package ru.round.shave.callback

object CancelAppointmentCallbackHandler :
    BaseCallbackHandler<Long>(Prefixes.CANCEL_APPOINTMENT_PREFIX) {

    override fun encodeData(data: Long): String {
        return data.toString()
    }

    override fun decodeData(value: String): Long {
        return value.toLong()
    }
}