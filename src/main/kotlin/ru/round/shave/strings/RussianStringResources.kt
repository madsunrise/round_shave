package ru.round.shave.strings

import ru.round.shave.Command
import ru.round.shave.entity.User

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

    override fun getMessageForConfirmation(
        serviceName: String,
        day: String,
        time: String,
        durationInMinutes: Int,
        price: String
    ): String {
        return listOf(
            "Услуга: $serviceName",
            "Дата и время: $day $time",
            "Длительность: $durationInMinutes мин.",
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

    override fun getRequestPhoneNumberMessage(): String {
        return "Оставьте, пожалуйста, свои контактные данные, чтобы я мог связаться с вами. " +
                "Для этого нажмите кнопку \"Поделиться\" ниже."
    }

    override fun getRequestPhoneNumberButtonText(): String {
        return "Поделиться"
    }

    override fun getRequestPhoneSuccessMessage(): String {
        return "Спасибо!"
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

    override fun getNewAppointmentAdminMessage(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int,
        user: User
    ): String {
        val list = mutableListOf(
            "Новая запись!",
            "--",
            "Услуга: $serviceName",
            "Длительность: $durationInMinutes мин.",
            "Стоимость: $totalPrice",
            "Дата и время: $day $time",
            "--",
        )
        list.addAll(constructUserInfoForAdmins(user))
        return list.joinToString(separator = "\n")
    }

    override fun getPhoneSharedAdminMessage(user: User): String {
        val list = mutableListOf(
            "Клиент поделился контактами.",
            "--"
        )
        list.addAll(constructUserInfoForAdmins(user))
        return list.joinToString(separator = "\n")
    }

    private fun constructUserInfoForAdmins(user: User): List<String> {
        val firstLine = StringBuilder(user.firstName)
        if (!user.lastName.isNullOrBlank()) {
            firstLine.append(' ')
            firstLine.append(user.lastName)
        }
        if (!user.username.isNullOrBlank()) {
            firstLine.append(" (@")
            firstLine.append(user.username)
            firstLine.append(')')
        }

        val result = mutableListOf<String>()
        result.add(firstLine.toString())

        if (!user.phone.isNullOrBlank()) {
            result.add("Телефон: +${user.phone}")
        }

        return result
    }

    override fun getCommandDescription(command: Command): String? {
        return when (command) {
            Command.START -> null
            Command.NEW_APPOINTMENT -> "Записаться на стрижку"
            Command.PRICE_LIST -> "Посмотреть цены"
            Command.MY_APPOINTMENTS -> "Мои записи"
        }
    }

    override fun getChooseAppointmentTypeText(): String {
        return "Какие записи вас интересуют?"
    }

    override fun getAppointmentsInPastButtonText(): String {
        return "Прошедшие"
    }

    override fun getAppointmentsInFutureButtonText(): String {
        return "Предстоящие"
    }

    override fun getNoAppointmentsFoundMessage(): String {
        return "Записей нет"
    }

    override fun getAppointmentDescription(serviceName: String, day: String, time: String, totalPrice: String): String {
        return mutableListOf(
            serviceName,
            "$day в $time",
            "Стоимость: $totalPrice"
        ).joinToString(separator = "\n")
    }
}
