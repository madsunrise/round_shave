package ru.round.shave

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.contact
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.round.shave.callback.*
import ru.round.shave.entity.Appointment
import ru.round.shave.entity.Back
import ru.round.shave.entity.Service
import ru.round.shave.exception.AlreadyExistException
import ru.round.shave.service.*
import ru.round.shave.strings.RussianStringResources
import ru.round.shave.strings.StringResources
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.PostConstruct

@Component
class RoundBot {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var serviceService: ServiceService

    @Autowired
    private lateinit var stateService: StateService

    @Autowired
    private lateinit var appointmentService: AppointmentService

    @Autowired
    private lateinit var timeManagementService: TimeManagementService

    private val stringResources: StringResources = RussianStringResources

    @PostConstruct
    fun run() {
        bot {
            logLevel = HttpLoggingInterceptor.Level.BODY
            token = System.getenv(TOKEN_ENVIRONMENT_VARIABLE)
            dispatch {
                command(Command.START.key) { bot, update ->
                    LOGGER.info("Handle start command")
                    val chatId = update.message!!.chat.id //ChatId.fromId(message.chat.id)
                    val tgUser = update.message!!.from!!
                    sendHelloMessage(bot, tgUser, chatId)
                    sendHelpMessage(bot, tgUser, chatId)
                }

                command(Command.NEW_APPOINTMENT.key) { bot, update ->
                    LOGGER.info("Handle new appointment command")
                    val chatId = update.message!!.chat.id //ChatId.fromId(message.chat.id)
                    val tgUser = update.message!!.from!!
                    suggestServicesForAppointment(bot, tgUser, chatId)
                }

                command(Command.PRICE_LIST.key) { bot, update ->
                    LOGGER.info("Handle price list command")
                    val chatId = update.message!!.chat.id //ChatId.fromId(message.chat.id)
                    val tgUser = update.message!!.from!!
                    sendPriceList(bot, tgUser, chatId)
                }

                command(Command.MY_APPOINTMENTS.key) { bot, update ->
                    LOGGER.info("Handle my appointments command")
                    val chatId = update.message!!.chat.id //ChatId.fromId(message.chat.id)
                    val tgUser = update.message!!.from!!
                    sendMyAppointmentsMessage(bot, tgUser, chatId)
                }

                callbackQuery(data = CALLBACK_DATA_RESET) { bot, update ->
                    resetEverything(bot, update.callbackQuery!!)
                }

                callbackQuery(data = CALLBACK_DATA_CONFIRM) { bot, update ->
                    handleConfirm(bot, update.callbackQuery!!)
                }

                callbackQuery(data = CALLBACK_DATA_APPOINTMENTS_IN_PAST) { bot, update ->
                    val chatId = update.callbackQuery!!.message!!.chat.id
                    val tgUser = update.callbackQuery!!.from
                    sendAppointmentsInPastMessage(bot, tgUser, chatId)
                }

                callbackQuery(data = CALLBACK_DATA_APPOINTMENTS_IN_FUTURE) { bot, update ->
                    val chatId = update.callbackQuery!!.message!!.chat.id
                    val tgUser = update.callbackQuery!!.from
                    sendAppointmentsInFutureMessage(bot, tgUser, chatId)
                }

                contact { bot, update, contact ->
                    LOGGER.info("Handle contact callback: $contact")
                    handlePhoneShared(bot, update.message!!.from!!, update.message!!.chat.id, contact)
                }

                callbackQuery { bot, update ->
                    val callbackQuery = update.callbackQuery!!
                    val callbackData = callbackQuery.data
                    val chatId = update.callbackQuery!!.message!!.chat.id
                    val tgUser = update.callbackQuery!!.from
                    when {
                        ChooseServiceCallbackHandler(serviceService).canHandle(callbackData) -> {
                            handleServiceChosen(bot, callbackQuery)
                        }
                        ChooseDateCallbackHandler.canHandle(callbackData) -> {
                            handleDayChosen(bot, callbackQuery)
                        }
                        ChooseTimeCallbackHandler.canHandle(callbackData) -> {
                            handleTimeChosen(bot, callbackQuery)
                        }
                        BackCallbackHandler.canHandle(callbackData) -> {
                            goBack(bot, callbackQuery)
                        }
                        CancelAppointmentCallbackHandler(appointmentService).canHandle(callbackData) -> {
                            askAboutAppointmentCancellation(bot, tgUser, chatId, callbackData)
                        }
                        CancelConfirmedAppointmentCallbackHandler(appointmentService).canHandle(callbackData) -> {
                            cancelAppointment(bot, tgUser, chatId, callbackData)
                        }
                    }
                }

                text { bot, update ->
                    val text = update.message!!.text
                    if (!text.isNullOrBlank() && Command.findCommand(text) == null) {
                        LOGGER.info("Received text: $text. Sending help.")
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        sendHelpMessage(bot, tgUser, chatId)
                    }
                }
            }
        }.startPolling()
    }

    private fun sendHelloMessage(bot: Bot, tgUser: User, chatId: Long) {
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getHelloMessage()
        )
    }

    private fun sendHelpMessage(bot: Bot, tgUser: User, chatId: Long) {
        val text = Command
            .values()
            .mapNotNull {
                val desc = stringResources.getCommandDescription(it)
                if (desc.isNullOrBlank()) {
                    null
                } else {
                    it to desc
                }
            }.joinToString("\n") { getCommandDescriptionWithKey(it.first.key, it.second) }

        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text
        )
    }

    private fun getCommandDescriptionWithKey(commandKey: String, description: String): String {
        return "/${commandKey} ${description.replaceFirstChar { it.lowercaseChar() }}"
    }

    private fun suggestServicesForAppointment(bot: Bot, tgUser: User, chatId: Long) {
        sendReplaceableMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getChooseServiceTypeMessage(),
            replyMarkup = createChooseServiceKeyboard()
        )
    }

    private fun sendPriceList(bot: Bot, tgUser: User, chatId: Long) {
        val text = serviceService.getAll().joinToString("\n") { it.getDisplayNameWithPrice() }
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text
        )
    }

    private fun sendMyAppointmentsMessage(bot: Bot, tgUser: User, chatId: Long) {
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getChooseAppointmentTypeText(),
            replyMarkup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton(
                            text = stringResources.getAppointmentsInPastButtonText(),
                            callbackData = CALLBACK_DATA_APPOINTMENTS_IN_PAST
                        ),
                        InlineKeyboardButton(
                            text = stringResources.getAppointmentsInFutureButtonText(),
                            callbackData = CALLBACK_DATA_APPOINTMENTS_IN_FUTURE
                        )
                    )
                )
            )
        )
    }

    private fun sendAppointmentsInPastMessage(bot: Bot, tgUser: User, chatId: Long) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Sending appointments in past message for ${user.getLogInfo()}")
        val appointments = appointmentService.getAppointmentsInPast(
            currentTime = getUserCurrentTime(),
            user = user,
            orderBy = AppointmentService.OrderBy.TIME_ASC
        )
        sendMessageWithAppointments(bot, tgUser, chatId, appointments, addCancelButton = false)
    }

    private fun sendAppointmentsInFutureMessage(bot: Bot, tgUser: User, chatId: Long) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Sending appointments in past message for ${user.getLogInfo()}")
        val appointments = appointmentService.getAppointmentsInFuture(
            currentTime = getUserCurrentTime(),
            user = user,
            orderBy = AppointmentService.OrderBy.TIME_ASC
        )
        sendMessageWithAppointments(bot, tgUser, chatId, appointments, addCancelButton = true)
    }

    private fun sendMessageWithAppointments(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        appointments: List<Appointment>,
        addCancelButton: Boolean
    ) {
        if (appointments.isEmpty()) {
            sendPersistentMessage(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = stringResources.getNoAppointmentsFoundMessage()
            )
            return
        }
        for (appointment in appointments) {
            val service = appointment.services.first()
            val text = stringResources.getAppointmentDescription(
                serviceName = service.getDisplayName(),
                day = appointment.startTime.format(VISIBLE_DATE_FORMATTER_FULL),
                time = appointment.startTime.format(VISIBLE_TIME_FORMATTER),
                totalPrice = service.getDisplayPrice()
            )
            sendPersistentMessage(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = text,
                replyMarkup = if (addCancelButton) {
                    InlineKeyboardMarkup.createSingleButton(createCancelAppointmentButton(appointment))
                } else {
                    null
                }
            )
        }
    }

    private fun createCancelAppointmentButton(appointment: Appointment): InlineKeyboardButton {
        val data = CancelAppointmentCallbackHandler(appointmentService).convertToCallbackData(appointment)
        return InlineKeyboardButton(
            text = stringResources.getCancelAppointmentButtonText(),
            callbackData = data
        )
    }

    private fun createCancelAppointmentConfirmButton(appointment: Appointment): InlineKeyboardButton {
        val data = CancelConfirmedAppointmentCallbackHandler(appointmentService).convertToCallbackData(appointment)
        return InlineKeyboardButton(
            text = stringResources.getCancelAppointmentConfirmButtonText(),
            callbackData = data
        )
    }

    private fun askAboutAppointmentCancellation(bot: Bot, tgUser: User, chatId: Long, callbackData: String) {
        val appointment = CancelAppointmentCallbackHandler(appointmentService).convertFromCallbackData(callbackData)
        val service = appointment.services.first()
        val text = stringResources.getCancelAppointmentConfirmationMessage(
            serviceName = service.getDisplayName(),
            day = appointment.startTime.format(VISIBLE_DATE_FORMATTER_FULL),
            time = appointment.startTime.format(VISIBLE_TIME_FORMATTER)
        )
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.createSingleButton(createCancelAppointmentConfirmButton(appointment))
        )
    }

    private fun cancelAppointment(bot: Bot, tgUser: User, chatId: Long, callbackData: String) {
        val appointment =
            CancelConfirmedAppointmentCallbackHandler(appointmentService).convertFromCallbackData(callbackData)
        val serviceName = appointment.services.first().getDisplayName()
        val day = appointment.startTime.format(VISIBLE_DATE_FORMATTER_FULL)
        val time = appointment.startTime.format(VISIBLE_TIME_FORMATTER)
        val text = stringResources.getAppointmentHasBeenCancelledMessage(
            day = day,
            time = time
        )
        appointmentService.delete(appointment)
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text
        )
        val user = userService.getOrCreate(tgUser, chatId)
        sendAppointmentCancelledAdminNotification(bot, serviceName, day, time, user)
    }

    private fun handleServiceChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenService = ChooseServiceCallbackHandler(serviceService).convertFromCallbackData(callbackQuery.data)
        handleServiceChosen(bot, callbackQuery, chosenService)
    }

    private fun handleServiceChosen(bot: Bot, callbackQuery: CallbackQuery, service: Service) {
        val chatId = callbackQuery.message!!.chat.id//ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        stateService.clearState(user)

        val freeDaysButtons = createChooseDayKeyboard(service.duration)
        if (freeDaysButtons.isEmpty()) {
            LOGGER.warn("Free days are not found!")
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getAppointmentTemporarilyUnavailableMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
        } else {
            val state = stateService.createNewState(user)
            stateService.handleServiceChosen(state, service)
            val withBackButton = freeDaysButtons.chunked(DAYS_PER_ROW) + listOf(createBackButton(Back.BACK_TO_SERVICES))
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getChooseDayMessage(service.getDisplayName(), service.duration),
                replyMarkup = InlineKeyboardMarkup(withBackButton)
            )
        }
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenDate = ChooseDateCallbackHandler.convertFromCallbackData(callbackQuery.data)
        handleDayChosen(bot, callbackQuery, chosenDate)
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery, day: LocalDate) {
        val chatId = callbackQuery.message!!.chat.id//ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        val state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("handleDayChosen: State is null!")
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
            return
        }

        val duration = state.service!!.duration
        val slots = timeManagementService.getFreeWindows(day, duration, getUserCurrentTime())
        if (slots.isEmpty()) {
            val freeDaysButtons = createChooseDayKeyboard(duration)
            val withBackButton = freeDaysButtons.chunked(DAYS_PER_ROW) + listOf(createBackButton(Back.BACK_TO_SERVICES))
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getChosenDayIsUnavailableMessage(VISIBLE_DATE_FORMATTER_FULL.format(day)),
                replyMarkup = InlineKeyboardMarkup(withBackButton)
            )
            return
        }

        stateService.handleDayChosen(state, day)

        val list = mutableListOf<InlineKeyboardButton>()
        for (slot in slots) {
            val callbackData = ChooseTimeCallbackHandler.convertToCallbackData(slot)
            val button = InlineKeyboardButton(
                text = VISIBLE_TIME_FORMATTER.format(slot),
                callbackData = callbackData
            )
            list.add(button)
        }

        val buttonsPerRow = 4
        while (list.size % buttonsPerRow != 0) {
            list.add(createStubButton())
        }

        val withBackButton = list.chunked(4) + listOf(createBackButton(Back.BACK_TO_CHOOSE_DAY))
        sendReplaceableMessage(
            bot = bot,
            tgUser = callbackQuery.from,
            chatId = chatId,
            text = stringResources.getChooseTimeMessage(
                serviceName = state.service.getDisplayName(),
                VISIBLE_DATE_FORMATTER_FULL.format(day)
            ),
            replyMarkup = InlineKeyboardMarkup(withBackButton)
        )
    }

    private fun createStubButton(): InlineKeyboardButton {
        return InlineKeyboardButton(text = " ", callbackData = createIgnoredCallbackData())
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenTime = ChooseTimeCallbackHandler.convertFromCallbackData(callbackQuery.data)
        handleTimeChosen(bot, callbackQuery, chosenTime)
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery, time: LocalTime) {
        val chatId = callbackQuery.message!!.chat.id//ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        var state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("handleTimeChosen: State is null!")
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
            return
        }

        state = stateService.handleTimeChosen(state, time)

        if (!stateService.isFilled(state)) {
            LOGGER.warn("State is not filled!")
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
            return
        }

        val keyboard = createConfirmKeyboard()
        val withBackButton = keyboard + listOf(createBackButton(Back.BACK_TO_CHOOSE_TIME))

        sendReplaceableMessage(
            bot = bot,
            tgUser = callbackQuery.from,
            chatId = chatId,
            text = stringResources.getMessageForConfirmation(
                serviceName = state.service!!.getDisplayName(),
                day = state.day!!.format(VISIBLE_DATE_FORMATTER_FULL),
                time = state.time!!.format(VISIBLE_TIME_FORMATTER),
                durationInMinutes = state.service!!.duration,
                price = state.service!!.getDisplayPrice()
            ),
            replyMarkup = InlineKeyboardMarkup(withBackButton)//InlineKeyboardMarkup.create(withBackButton)
        )
    }

    private fun createChooseServiceKeyboard(): ReplyMarkup {
        val buttons = serviceService.getAll().map {
            val callbackData = ChooseServiceCallbackHandler(serviceService).convertToCallbackData(it)
            //InlineKeyboardButton.CallbackData(it.getDisplayNameWithPrice(), callbackData)
            InlineKeyboardButton(text = it.getDisplayName(), callbackData = callbackData)
        }
        //return InlineKeyboardMarkup.create(buttons.map { listOf(it) })
        return InlineKeyboardMarkup(buttons.map { listOf(it) })
    }

    private fun createChooseDayKeyboard(requiredDuration: Int): List<InlineKeyboardButton> {
        val daysAvailable = 12
        val list = mutableListOf<InlineKeyboardButton>()
        timeManagementService.getDaysThatHaveFreeWindows(daysAvailable, requiredDuration, getUserCurrentTime())
            .forEach { current ->
                val callbackData = ChooseDateCallbackHandler.convertToCallbackData(current)
                val button =
                    InlineKeyboardButton(text = VISIBLE_DATE_FORMATTER.format(current), callbackData = callbackData)
                list.add(button)
            }
        return list
    }

    private fun createConfirmKeyboard(): List<List<InlineKeyboardButton>> {
        val confirmButton = InlineKeyboardButton(
            text = stringResources.getConfirmButtonText(),
            callbackData = CALLBACK_DATA_CONFIRM
        )
        val resetButton = createGoToBeginningButton()
        return listOf(listOf(resetButton, confirmButton))
    }

    private fun handleConfirm(bot: Bot, callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message!!.chat.id//ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        var state = stateService.getUserState(user)
        if (state == null || !stateService.isFilled(state)) {
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
            return
        }
        LOGGER.info("Confirmed state for user ${user.id}: $state")
        val service = state.service!!
        val startTime = LocalDateTime.of(state.day, state.time)

        if (startTime < getUserCurrentTime()) {
            LOGGER.warn("Chosen time is in the past!")
            sendPersistentMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getChosenTimeIsInThePastMessage()
            )
            state = stateService.applyBack(state, Back.BACK_TO_CHOOSE_TIME)
            handleDayChosen(bot, callbackQuery, state.day!!)
            return
        }

        val appointment = Appointment(
            user = user,
            services = listOf(service),
            startTime = startTime,
            endTime = startTime.plusMinutes(service.duration.toLong())
        )
        try {
            appointmentService.insert(appointment)
            LOGGER.info("New appointment: $appointment")
            stateService.clearState(user)

            val serviceName = service.getDisplayName()
            val day = VISIBLE_DATE_FORMATTER_FULL.format(state.day)
            val time = VISIBLE_TIME_FORMATTER.format(startTime)
            val totalPrice = service.getDisplayPrice()
            val durationInMinutes = service.duration

            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getConfirmedMessage(
                    serviceName = serviceName,
                    day = day,
                    time = time,
                    totalPrice = totalPrice,
                    durationInMinutes = durationInMinutes
                ),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                keepMessage = true
            )
            sendNewAppointmentAdminNotification(
                bot = bot,
                serviceName = serviceName,
                day = day,
                time = time,
                totalPrice = totalPrice,
                durationInMinutes = durationInMinutes,
                user = user
            )
            if (user.phone.isNullOrBlank()) {
                LOGGER.info("Requesting phone number")
                requestPhoneNumber(bot, callbackQuery.from, chatId)
            }
        } catch (e: AlreadyExistException) {
            LOGGER.warn("Time is already taken!")
            sendPersistentMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getChosenTimeIsAlreadyTakenMessage(VISIBLE_TIME_FORMATTER.format(startTime))
            )
            state = stateService.applyBack(state, Back.BACK_TO_CHOOSE_TIME)
            handleDayChosen(bot, callbackQuery, state.day!!)
        } catch (e: Exception) {
            LOGGER.error("Error!", e)
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
            )
        }
    }

    private fun requestPhoneNumber(bot: Bot, tgUser: User, chatId: Long) {
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getRequestPhoneNumberMessage(),
            replyMarkup = KeyboardReplyMarkup(
                keyboard = arrayOf(
                    KeyboardButton(
                        text = stringResources.getRequestPhoneNumberButtonText(),
                        requestContact = true
                    )
                ),
                resizeKeyboard = true
            )
        )
    }

    private fun handlePhoneShared(bot: Bot, tgUser: User, chatId: Long, contact: Contact) {
        val user = userService.getOrCreate(tgUser, chatId).copy(phone = contact.phoneNumber)
        userService.update(user)
        LOGGER.info("Added phone number for user ${user.getLogInfo()}")
        sendReplaceableMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getRequestPhoneSuccessMessage(),
            replyMarkup = ReplyKeyboardRemove()
        )
        sendPhoneSharedAdminNotification(bot, user)
    }

    private fun createBackButton(back: Back): List<InlineKeyboardButton> {
        val callback = BackCallbackHandler.convertToCallbackData(back)
        //return listOf(InlineKeyboardButton.CallbackData(stringResources.getBackButtonText(), callback))
        return listOf(InlineKeyboardButton(text = stringResources.getBackButtonText(), callbackData = callback))
    }

    private fun goBack(bot: Bot, callbackQuery: CallbackQuery) {
        val back = BackCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val chatId = callbackQuery.message!!.chat.id //ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        LOGGER.info("Go back: $back from user ${user.id}")
        var state = stateService.getUserState(user)
        if (state == null) {
            LOGGER.warn("Failed to apply back :$back: state is null")
            resetEverything(bot, callbackQuery)
            return
        }
        state = stateService.applyBack(state, back)
        val res: Any = when (back) {
            Back.BACK_TO_SERVICES -> {
                resetEverything(bot, callbackQuery)
            }
            Back.BACK_TO_CHOOSE_DAY -> {
                val service = state.service
                if (service == null) {
                    LOGGER.warn("Service is null!")
                    stateService.clearState(user)
                    sendReplaceableMessage(
                        bot = bot,
                        tgUser = callbackQuery.from,
                        chatId = chatId,
                        text = stringResources.getErrorMessage(),
                        replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
                    )
                } else {
                    handleServiceChosen(bot, callbackQuery, service)
                }
            }
            Back.BACK_TO_CHOOSE_TIME -> {
                val day = state.day
                if (day == null) {
                    LOGGER.warn("Day is null!")
                    stateService.clearState(user)
                    sendReplaceableMessage(
                        bot = bot,
                        tgUser = callbackQuery.from,
                        chatId = chatId,
                        text = stringResources.getErrorMessage(),
                        replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton())
                    )
                } else {
                    handleDayChosen(bot, callbackQuery, day)
                }
            }
        }
    }

    private fun resetEverything(bot: Bot, callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message!!.chat.id//ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        stateService.clearState(user)
        suggestServicesForAppointment(bot, callbackQuery.from, chatId)
    }

    private fun createGoToBeginningButton(): InlineKeyboardButton {
        //return InlineKeyboardButton.CallbackData(stringResources.getGoToBeginningButtonText(), CALLBACK_DATA_RESET)
        return InlineKeyboardButton(
            text = stringResources.getGoToBeginningButtonText(),
            callbackData = CALLBACK_DATA_RESET
        )
    }

    private fun sendNewAppointmentAdminNotification(
        bot: Bot,
        serviceName: String,
        day: String,
        time: String,
        totalPrice: String,
        durationInMinutes: Int,
        user: ru.round.shave.entity.User
    ) {
        val admins = getAdminUsers()
        if (admins.isEmpty()) {
            LOGGER.warn("No admins found!")
            return
        }
        val text = stringResources.getNewAppointmentAdminMessage(
            serviceName = serviceName,
            day = day,
            time = time,
            totalPrice = totalPrice,
            durationInMinutes = durationInMinutes,
            user = user
        )
        for (admin in admins) {
            bot.sendMessage(
                chatId = admin.chatId,
                text = text
            )
        }
    }

    private fun sendAppointmentCancelledAdminNotification(
        bot: Bot,
        serviceName: String,
        day: String,
        time: String,
        user: ru.round.shave.entity.User
    ) {
        val admins = getAdminUsers()
        if (admins.isEmpty()) {
            LOGGER.warn("No admins found!")
            return
        }

        val text = stringResources.getAppointmentHasBeenCancelledAdminMessage(
            serviceName = serviceName,
            day = day,
            time = time,
            user = user
        )
        for (admin in admins) {
            bot.sendMessage(
                chatId = admin.chatId,
                text = text
            )
        }
    }

    private fun sendPhoneSharedAdminNotification(
        bot: Bot,
        user: ru.round.shave.entity.User
    ) {
        val admins = getAdminUsers()
        if (admins.isEmpty()) {
            LOGGER.warn("No admins found!")
            return
        }
        val text = stringResources.getPhoneSharedAdminMessage(user = user)
        for (admin in admins) {
            bot.sendMessage(
                chatId = admin.chatId,
                text = text
            )
        }
    }

    private fun sendReplaceableMessage(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        text: String,
        replyMarkup: ReplyMarkup? = null,
        keepMessage: Boolean = false // pass true if you want to prevent replacing this message in future
    ) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Send replaceable message called! User = ${user.getLogInfo()}, text=$text, keepMessage=$keepMessage")
        if (user.replaceableMessageId == null) {
            val msgId = sendNewMessageInternal(bot, user, text, replyMarkup)
            if (msgId != null && !keepMessage) {
                LOGGER.info("Updating replaceable message ID for user ${user.getLogInfo()}")
                val copy = user.copy(replaceableMessageId = msgId)
                userService.update(copy)
            }
            return
        }

        val res = bot.editMessageText(
            chatId = user.chatId,
            messageId = user.replaceableMessageId,
            text = text,
            replyMarkup = replyMarkup
        )

        if (res.first?.isSuccessful != true) {
            val msgId = sendNewMessageInternal(bot, user, text, replyMarkup)
            if (msgId != null && !keepMessage) {
                userService.update(user.copy(replaceableMessageId = msgId))
            }
        }

        if (keepMessage) {
            userService.update(user.copy(replaceableMessageId = null))
        }
    }

    private fun sendPersistentMessage(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        text: String,
        replyMarkup: ReplyMarkup? = null
    ) {
        val user = userService.getOrCreate(tgUser, chatId)
        sendNewMessageInternal(bot, user, text, replyMarkup)
        // Reset replaceableMessageId to continue conversation below current message
        userService.update(user.copy(replaceableMessageId = null))
    }

    private fun sendNewMessageInternal(
        bot: Bot,
        user: ru.round.shave.entity.User,
        text: String,
        replyMarkup: ReplyMarkup? = null
    ): Long? {
        val response = bot.sendMessage(
            chatId = user.chatId,
            text = text,
            replyMarkup = replyMarkup
        )
        return response.first?.body()?.result?.messageId
    }

    private fun getUserCurrentTime(): LocalDateTime {
        return LocalDateTime.now(TIME_ZONE)
    }

    private fun createIgnoredCallbackData(): String {
        return UUID.randomUUID().toString()
    }

    private fun getAdminUsers(): List<ru.round.shave.entity.User> {
        return ADMIN_USER_IDS.mapNotNull { userService.getById(it) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RoundBot::class.java.simpleName)
        private const val TOKEN_ENVIRONMENT_VARIABLE = "ROUND_SHAVE_TOKEN"
        val TIME_ZONE: ZoneId = ZoneId.of("Europe/Moscow")

        private val VISIBLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM")
        private val VISIBLE_DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val VISIBLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH.mm")

        private const val CALLBACK_DATA_RESET = "callback_data_reset"
        private const val CALLBACK_DATA_CONFIRM = "callback_data_confirm"
        private const val CALLBACK_DATA_APPOINTMENTS_IN_PAST = "appointments_in_past"
        private const val CALLBACK_DATA_APPOINTMENTS_IN_FUTURE = "appointments_in_future"

        private const val DAYS_PER_ROW = 4

        private val ADMIN_USER_IDS = arrayOf(225893185L)
    }
}