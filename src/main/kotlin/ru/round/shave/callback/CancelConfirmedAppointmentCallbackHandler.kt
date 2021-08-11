package ru.round.shave.callback

import ru.round.shave.entity.Appointment
import ru.round.shave.service.AppointmentService

class CancelConfirmedAppointmentCallbackHandler(private val appointmentService: AppointmentService) :
    BaseCallbackHandler<Appointment>(Prefixes.CANCEL_CONFIRMED_APPOINTMENT_PREFIX) {

    override fun encodeData(data: Appointment): String {
        return data.id.toString()
    }

    override fun decodeData(value: String): Appointment {
        return appointmentService.getById(value.toLong())!!
    }
}