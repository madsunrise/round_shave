package ru.round.shave.strings

import ru.round.shave.Command
import ru.round.shave.entity.User

interface StringResources {
    fun getHelloMessage(): String

    fun getErrorMessage(): String

    fun getChooseServiceTypeMessage(): String

    fun getChooseVisitDayMessage(): String

    fun getAppointmentTemporarilyUnavailableMessage(): String

    fun getChosenDayIsUnavailableMessage(date: String): String

    fun getChooseDayMessage(serviceName: String, durationInMinutes: Int): String

    fun getChooseTimeMessage(serviceName: String, day: String): String

    fun getMessageForConfirmation(
        serviceName: String,
        day: String,
        time: String,
        durationInMinutes: Int,
        price: String
    ): String

    fun getConfirmButtonText(): String

    fun getChosenTimeIsAlreadyTakenMessage(time: String): String

    fun getChosenTimeIsInThePastMessage(): String

    fun getRequestPhoneNumberMessage(): String

    fun getRequestPhoneNumberButtonText(): String

    fun getRequestPhoneSuccessMessage(): String

    fun getConfirmedMessage(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int
    ): String

    fun getBackButtonText(): String

    fun getGoToBeginningButtonText(): String

    fun getNewAppointmentAdminMessage(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int,
        user: User
    ): String

    fun getPhoneSharedAdminMessage(user: User): String

    fun getCommandDescription(command: Command): String?

    fun getChooseAppointmentTypeText(): String

    fun getAppointmentsInPastButtonText(): String

    fun getAppointmentsInFutureButtonText(): String

    fun getNoAppointmentsFoundMessage(): String

    fun getAppointmentDescription(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String
    ): String

    fun getAppointmentDescriptionForAdmin(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        user: User
    ): String

    fun getCancelAppointmentButtonText(): String

    fun getAppointmentNotFoundMessage(): String

    fun getCancelAppointmentConfirmationMessage(
        serviceName: String,
        day: String,
        time: String
    ): String

    fun getCancelAppointmentConfirmButtonText(): String

    fun getAppointmentHasBeenCancelledMessage(day: String, time: String): String

    fun getAppointmentHasBeenCancelledAdminMessage(
        serviceName: String,
        day: String,
        time: String,
        user: User
    ): String

    fun getRemindBeforeTwoHoursText(time: String, serviceName: String): String
}
