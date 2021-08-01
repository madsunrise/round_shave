package ru.round.shave

object RussianStringResources : StringResources {

    private val address: String = "2-я Магистральная ул., 3с3."

    override fun getHelloMessage(): String {
        return "Вас приветствует Round Shave Bot!"
    }

    override fun getErrorMessage(): String {
        return "К сожалению, произошла ошибка. Повторите процедуру записи."
    }

    override fun getChooseServiceTypeMessage(): String {
        return "Выберите тип услуги."
    }

    override fun getChooseVisitDayMessage(): String {
        return "Выберите день посещения."
    }

    override fun getAppointmentTemporarilyUnavailableMessage(): String {
        return "К сожалению, запись временно недоступна."
    }

    override fun getChosenDayIsUnavailableMessage(date: String): String {
        return "К сожалению, запись на $date недоступна. Выберите другую дату."
    }

    override fun getChooseDayMessage(serviceName: String, durationInMinutes: Int): String {
        return listOf(
            "Выбранная услуга: $serviceName",
            "Длительность: $durationInMinutes мин.",
            "",
            "Выберите день посещения.",
        ).joinToString(separator = "\n")
    }

    override fun getChooseTimeMessage(serviceName: String, day: String): String {
        return listOf(
            serviceName,
            "Выбранный день: $day",
            "",
            "Выберите время посещения.",
        ).joinToString(separator = "\n")
    }

    override fun getMessageForConfirmation(serviceName: String, day: String, time: String, price: String): String {
        return listOf(
            "Услуга: $serviceName",
            "Дата и время: $day $time",
            "Стоимость: $price",
            "",
            "Всё верно?",
        ).joinToString(separator = "\n")
    }

    override fun getConfirmButtonText(): String {
        return "Записаться"
    }

    override fun getTimeIsAlreadyTakenMessage(time: String): String {
        return "К сожалению, выбранное время ($time) уже занято. Выберите другое время."
    }

    override fun getConfirmedMessage(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int
    ): String {
        return listOf(
            "Запись подтверждена. Ждём вас $day в $time по адресу: $address",
            "",
            "Услуга: $serviceName",
            "Длительность: $durationInMinutes мин.",
            "",
            "Итоговая стоимость: $totalPrice"
        ).joinToString(separator = "\n")
    }

    override fun getBackButtonText(): String {
        return "Назад"
    }

    override fun getGoToBeginningButtonText(): String {
        return "В начало"
    }
}
