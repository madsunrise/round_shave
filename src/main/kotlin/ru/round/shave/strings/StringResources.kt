package ru.round.shave.strings

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

    fun getTimeIsAlreadyTakenMessage(time: String): String

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
}