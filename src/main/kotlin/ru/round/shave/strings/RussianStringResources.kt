package ru.round.shave.strings

import ru.round.shave.Command
import ru.round.shave.entity.User

object RussianStringResources : StringResources {

    private const val address: String = "2-я Магистральная ул., 3с3."

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

    override fun getChosenTimeIsAlreadyTakenMessage(time: String): String {
        return "К сожалению, выбранное время ($time) уже занято. Выберите другое время."
    }

    override fun getChosenTimeIsInThePastMessage(): String {
        return "Запись невозможна, так как выбрано время в прошлом."
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
            Command.MASTER_CONTACTS -> "Контакты мастера"
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

    override fun getAppointmentsInPastMessage(): String {
        return "Прошедшие записи:"
    }

    override fun getAppointmentsInPastNotFoundMessage(): String {
        return "Прошедших записей не найдено."
    }

    override fun getAppointmentsInFutureMessage(): String {
        return "Предстоящие записи:"
    }

    override fun getAppointmentsInFutureNotFoundMessage(): String {
        return "Предстоящих записей не найдено."
    }

    override fun getAppointmentDescription(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int
    ): String {
        return mutableListOf(
            serviceName,
            "$day в $time",
            "Стоимость: $totalPrice",
            "Длительность: $durationInMinutes мин.",
        ).joinToString(separator = "\n")
    }

    override fun getAppointmentDescriptionForAdmin(
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int,
        user: User
    ): String {
        val list = mutableListOf(
            serviceName,
            "$day в $time",
            "Стоимость: $totalPrice",
            "Длительность: $durationInMinutes мин.",
            "--"
        )
        list.addAll(constructUserInfoForAdmins(user))
        return list.joinToString(separator = "\n")
    }

    override fun getCancelAppointmentButtonText(): String {
        return "Отменить"
    }

    override fun getAppointmentNotFoundMessage(): String {
        return "Запись не найдена."
    }

    override fun getCancelAppointmentConfirmationMessage(serviceName: String, day: String, time: String): String {
        return mutableListOf(
            "Вы уверены, что хотите отменить запись на $day $time?",
            "",
            "Услуга: $serviceName"
        ).joinToString(separator = "\n")
    }

    override fun getCancelAppointmentConfirmButtonText(): String {
        return "Да, отменить"
    }

    override fun getAppointmentHasBeenCancelledMessage(day: String, time: String): String {
        return "Запись на $day $time отменена."
    }

    override fun getAppointmentHasBeenCancelledAdminMessage(
        serviceName: String,
        day: String,
        time: String,
        user: User
    ): String {
        val list = mutableListOf(
            "Запись на $day $time отменена.",
            "--",
            "Услуга: $serviceName",
            "--",
        )
        list.addAll(constructUserInfoForAdmins(user))
        return list.joinToString(separator = "\n")
    }

    override fun getRemindBeforeTwoHoursText(time: String, serviceName: String): String {
        return mutableListOf(
            "Напоминание о записи.",
            "--",
            "Ждём вас в $time по адресу: $address.",
            "Услуга: ${serviceName.replaceFirstChar { it.lowercase() }}",
            "--",
            "Если у вас не получается приехать, то вы можете отменить запись в " +
                    "разделе \"Мои записи\" (/${Command.MY_APPOINTMENTS.key})."
        ).joinToString(separator = "\n")
    }

    override fun getMasterContacts(): String {
        return mutableListOf(
            MasterInfo.name,
            "--",
            "Телефон: ${MasterInfo.phone}",
            "WhatsApp: ${MasterInfo.whatsapp}",
            "Telegram: ${MasterInfo.telegram}",
            "--",
            "Адрес: $address"
        ).joinToString(separator = "\n")
    }

    override fun getSendLocationButtonText(): String {
        return "Карта"
    }
}
