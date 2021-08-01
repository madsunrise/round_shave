package ru.round.shave

interface StringResources {
    fun getHelloMessage(): String

    fun getErrorMessage(): String

    fun getChooseServiceTypeMessage(): String

    fun getChooseVisitDayMessage(): String

    fun getAppointmentTemporarilyUnavailableMessage(): String

    fun getChosenDayIsUnavailableMessage(date: String): String

    fun getChooseDayMessage(serviceName: String): String

    fun getChooseTimeMessage(serviceName: String, day: String): String

    fun getMessageForConfirmation(serviceName: String, day: String, time: String, price: String): String

    fun getConfirmButtonText(): String

    fun getTimeIsAlreadyTakenMessage(time: String): String

    fun getConfirmedMessage(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int
    ): String

    fun getBackButtonText(): String

    fun getGoToBeginningButtonText(): String
}
