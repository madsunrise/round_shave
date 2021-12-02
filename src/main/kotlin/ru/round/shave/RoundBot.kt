package ru.round.shave

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.round.shave.callback.*
import ru.round.shave.entity.Appointment
import ru.round.shave.entity.Back
import ru.round.shave.entity.Service
import ru.round.shave.entity.WorkingHours
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
import java.util.regex.Pattern
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

    @Autowired
    private lateinit var workingHoursService: WorkingHoursService

    private val stringResources: StringResources = RussianStringResources

    private lateinit var bot: Bot

    @PostConstruct
    fun run() {
        bot = bot {
            logLevel = LogLevel.None
            token = System.getenv(TOKEN_ENVIRONMENT_VARIABLE)
            dispatch {
                command(Command.START.key) {
                    LOGGER.info("Handle start command")
                    try {
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        sendHelloMessage(bot, tgUser, chatId)
                        sendHelpMessage(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                command(Command.NEW_APPOINTMENT.key) {
                    LOGGER.info("Handle new appointment command")
                    try {
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        suggestServicesForAppointment(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                command(Command.PRICE_LIST.key) {
                    LOGGER.info("Handle price list command")
                    try {
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        sendPriceList(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                command(Command.MY_APPOINTMENTS.key) {
                    LOGGER.info("Handle my appointments command")
                    try {
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        sendMyAppointmentsMessage(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                command(Command.MASTER_CONTACTS.key) {
                    LOGGER.info("Handle master contacts command")
                    try {
                        val chatId = update.message!!.chat.id
                        val tgUser = update.message!!.from!!
                        sendMessageWithMasterContacts(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_RESET) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        resetEverything(bot, update.callbackQuery!!)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_CONFIRM) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        handleConfirm(bot, update.callbackQuery!!)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_APPOINTMENTS_IN_PAST) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        val chatId = update.callbackQuery!!.message!!.chat.id
                        val tgUser = update.callbackQuery!!.from
                        sendAppointmentsInPastMessage(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_APPOINTMENTS_IN_FUTURE) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        val chatId = update.callbackQuery!!.message!!.chat.id
                        val tgUser = update.callbackQuery!!.from
                        sendAppointmentsInFutureMessage(bot, tgUser, chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_MY_FREE_WINDOWS) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        val chatId = update.callbackQuery!!.message!!.chat.id
                        val tgUser = update.callbackQuery!!.from
                        if (isAdmin(tgUser.id)) {
                            sendFreeWindowsToAdmin(bot, tgUser, chatId)
                        }
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery(data = CALLBACK_DATA_SEND_LOCATION) {
                    try {
                        LOGGER.info("Callback query: ${update.callbackQuery?.data}")
                        val chatId = update.callbackQuery!!.message!!.chat.id
                        val tgUser = update.callbackQuery!!.from
                        // clear button "Location"
                        clearLastMessageReplyMarkup(bot, tgUser, chatId)
                        sendLocation(chatId)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                contact {
                    LOGGER.info("Handle contact callback: $contact")
                    try {
                        handlePhoneShared(bot, update.message!!.from!!, update.message!!.chat.id, contact)
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                callbackQuery {
                    try {
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
                            ChooseDateForFreeWindowsCallbackHandler.canHandle(callbackData) -> {
                                handleAdminHasChosenDayForFreeWindows(bot, callbackQuery)
                            }
                            ChooseTimeCallbackHandler.canHandle(callbackData) -> {
                                handleTimeChosen(bot, callbackQuery)
                            }
                            BackCallbackHandler.canHandle(callbackData) -> {
                                goBack(bot, callbackQuery)
                            }
                            CancelAppointmentCallbackHandler.canHandle(callbackData) -> {
                                askAboutAppointmentCancellation(bot, tgUser, chatId, callbackData)
                            }
                            CancelConfirmedAppointmentCallbackHandler.canHandle(callbackData) -> {
                                cancelAppointment(bot, tgUser, chatId, callbackData)
                            }
                        }
                    } catch (t: Throwable) {
                        LOGGER.error("Error occurred!", t)
                        sendErrorToDeveloper(bot, t.message ?: "unknown")
                    }
                }

                text {
                    val text = update.message!!.text
                    if (text.isNullOrBlank()) {
                        return@text
                    }
                    val tgUser = update.message!!.from!!
                    val chatId = update.message!!.chat.id

                    val startedWithDateRegex = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4} .*")
                    if (startedWithDateRegex.matcher(text).matches() && isAdmin(tgUser.id)) {
                        try {
                            processWorkingHoursChangeMessage(text, tgUser, chatId)
                        } catch (e: Exception) {
                            LOGGER.warn("Failed to change working hours", e)
                            sendPersistentMessage(
                                bot = bot,
                                tgUser = tgUser,
                                chatId = chatId,
                                text = stringResources.getUnknownErrorMessage(),
                                clearLastMessageReplyMarkup = true
                            )

                        }
                        return@text
                    }

                    if (Command.findCommand(text) == null) {
                        LOGGER.info("Received text: $text. Sending help.")
                        if (!isAdmin(tgUser.id)) {
                            sendHelpMessage(bot, tgUser, chatId)
                        }
                    }
                }

                telegramError {
                    LOGGER.error("Telegram error handled: $error")
                    sendErrorToDeveloper(bot, error.getErrorMessage())
                }
            }
        }
        bot.startPolling()
    }

    private fun processWorkingHoursChangeMessage(text: String, tgUser: User, chatId: Long) {
        val split = text.split(" ")
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dateStr = split.first()
        val date = LocalDate.parse(dateStr, formatter)
        val message: String
        if (split[1] == "-") {
            // Removing working day
            val hours = workingHoursService.getWorkingHours(date)
            message = if (hours == null) {
                stringResources.getAdminMessageCouldNotDeleteWorkingHoursAsItNotExists(dateStr)
            } else {
                workingHoursService.delete(hours)
                stringResources.getAdminMessageDeletingWorkingHoursSuccessful(dateStr)
            }
        } else {
            // Adding new working day
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val existing = workingHoursService.getWorkingHours(date)
            if (existing != null) {
                message = stringResources.getAdminMessageCouldNotAddWorkingHoursAsItAlreadyExists(
                    day = dateStr,
                    startTime = existing.startTime.format(timeFormatter),
                    endTime = existing.endTime.format(timeFormatter)
                )
            } else {
                val timeStartStr = split[1]
                val timeEndStr = split[2]
                val timeStart = LocalTime.parse(timeStartStr, timeFormatter)
                val timeEnd = LocalTime.parse(timeEndStr, timeFormatter)
                val entity = WorkingHours(day = date, startTime = timeStart, endTime = timeEnd)
                workingHoursService.insert(entity)
                message = stringResources.getAdminMessageAddingWorkingHoursSuccessful(
                    day = dateStr,
                    startTime = timeStartStr,
                    endTime = timeEndStr
                )
            }
        }

        sendPersistentMessage(
            bot,
            tgUser,
            chatId,
            message,
            clearLastMessageReplyMarkup = true
        )

        // Just for stat
        sendToDeveloper(bot, message)
    }

    private fun sendHelloMessage(bot: Bot, tgUser: User, chatId: Long) {
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getHelloMessage(),
            clearLastMessageReplyMarkup = true
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
            text = text,
            clearLastMessageReplyMarkup = true
        )
    }

    private fun getCommandDescriptionWithKey(commandKey: String, description: String): String {
        return "/${commandKey} - ${description.replaceFirstChar { it.lowercaseChar() }}"
    }

    private fun suggestServicesForAppointment(bot: Bot, tgUser: User, chatId: Long) {
        sendReplaceableMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getChooseServiceTypeMessage(),
            replyMarkup = createChooseServiceKeyboard(),
            clearLastMessageReplyMarkup = true,
            keepMessage = false
        )
    }

    private fun sendPriceList(bot: Bot, tgUser: User, chatId: Long) {
        val text = serviceService.getAll().joinToString("\n") {
            "${it.getDisplayName()} *${it.getDisplayPrice()}*"
        }
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text,
            clearLastMessageReplyMarkup = true,
            parseMode = ParseMode.MARKDOWN
        )
    }

    private fun sendMyAppointmentsMessage(bot: Bot, tgUser: User, chatId: Long) {
        val buttons = mutableListOf(
            InlineKeyboardButton.CallbackData(
                text = stringResources.getAppointmentsInPastButtonText(),
                callbackData = CALLBACK_DATA_APPOINTMENTS_IN_PAST
            ),
            InlineKeyboardButton.CallbackData(
                text = stringResources.getAppointmentsInFutureButtonText(),
                callbackData = CALLBACK_DATA_APPOINTMENTS_IN_FUTURE
            )
        )
        if (isAdmin(tgUser.id)) {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = stringResources.getMyFreeWindowsButtonText(),
                    callbackData = CALLBACK_DATA_MY_FREE_WINDOWS
                )
            )
        }
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getChooseAppointmentTypeText(),
            clearLastMessageReplyMarkup = true,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(buttons)
            )
        )
    }

    private fun sendAppointmentsInPastMessage(bot: Bot, tgUser: User, chatId: Long) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Sending appointments in past for ${user.getLogInfo()}")
        var appointments = appointmentService.getAppointmentsInPast(
            currentTime = getUserCurrentTime(),
            user = if (isAdmin(user.id)) null else user,
            orderBy = AppointmentService.OrderBy.TIME_ASC
        )
        if (appointments.isEmpty()) {
            sendPersistentMessage(
                bot = bot,
                chatId = chatId,
                tgUser = tgUser,
                text = stringResources.getAppointmentsInPastNotFoundMessage(),
                clearLastMessageReplyMarkup = false,
            )
        } else {
            sendPersistentMessage(
                bot = bot,
                chatId = chatId,
                tgUser = tgUser,
                text = stringResources.getAppointmentsInPastMessage(),
                clearLastMessageReplyMarkup = false,
            )
            if (isAdmin(user.id)) {
                val maxRecords = 50
                if (appointments.size > maxRecords) {
                    appointments = appointments.subList(0, maxRecords)
                    sendPersistentMessage(
                        bot = bot,
                        chatId = chatId,
                        tgUser = tgUser,
                        text = stringResources.getShownLastNAppointments(maxRecords),
                        clearLastMessageReplyMarkup = false,
                    )
                }
            }
            sendMessageWithAppointments(bot, tgUser, chatId, appointments, addCancelButton = false)
        }
    }

    private fun sendAppointmentsInFutureMessage(bot: Bot, tgUser: User, chatId: Long) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Sending appointments in future for ${user.getLogInfo()}")
        val appointments = appointmentService.getAppointmentsInFuture(
            currentTime = getUserCurrentTime(),
            user = if (isAdmin(user.id)) null else user,
            orderBy = AppointmentService.OrderBy.TIME_ASC
        )
        if (appointments.isEmpty()) {
            sendPersistentMessage(
                bot = bot,
                chatId = chatId,
                tgUser = tgUser,
                text = stringResources.getAppointmentsInFutureNotFoundMessage(),
                clearLastMessageReplyMarkup = false,
            )
        } else {
            sendPersistentMessage(
                bot = bot,
                chatId = chatId,
                tgUser = tgUser,
                text = stringResources.getAppointmentsInFutureMessage(),
                clearLastMessageReplyMarkup = false,
            )
            sendMessageWithAppointments(bot, tgUser, chatId, appointments, addCancelButton = true)
        }
    }

    private fun sendFreeWindowsToAdmin(bot: Bot, tgUser: User, chatId: Long) {
        val now = LocalDate.now(TIME_ZONE)
        val workingHours = workingHoursService.getAll().filter { it.day >= now }
        if (workingHours.isEmpty()) {
            sendPersistentMessage(
                bot = bot,
                chatId = chatId,
                tgUser = tgUser,
                text = stringResources.getNoWorkingHoursFoundAdminMessage(),
                clearLastMessageReplyMarkup = false,
            )
            return
        }

        val buttons = workingHours.map {
            val day = it.day
            val callbackData = ChooseDateForFreeWindowsCallbackHandler.convertToCallbackData(day)
            InlineKeyboardButton.CallbackData(
                text = VISIBLE_DATE_FORMATTER.format(day),
                callbackData = callbackData
            )
        }

        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getChooseDayForFreeWindowsAdminMessage(),
            replyMarkup = InlineKeyboardMarkup.create(buttons.chunked(DAYS_PER_ROW)),
            clearLastMessageReplyMarkup = true
        )
    }

    private fun sendMessageWithAppointments(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        appointments: List<Appointment>,
        addCancelButton: Boolean
    ) {
        for (appointment in appointments) {
            val service = appointment.services.first()
            val text = if (isAdmin(tgUser.id)) {
                stringResources.getAppointmentDescriptionForAdmin(
                    serviceName = service.getDisplayName(),
                    day = appointment.startTime.format(VISIBLE_DATE_FORMATTER_FULL),
                    time = appointment.startTime.format(VISIBLE_TIME_FORMATTER),
                    totalPrice = service.getDisplayPrice(),
                    durationInMinutes = service.duration,
                    user = appointment.user
                )
            } else {
                stringResources.getAppointmentDescription(
                    serviceName = service.getDisplayName(),
                    day = appointment.startTime.format(VISIBLE_DATE_FORMATTER_FULL),
                    time = appointment.startTime.format(VISIBLE_TIME_FORMATTER),
                    totalPrice = service.getDisplayPrice(),
                    durationInMinutes = service.duration,
                )
            }
            val showCancelButton = addCancelButton && (!isAdmin(tgUser.id) || tgUser.id == appointment.user.id)
            sendPersistentMessage(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = text,
                clearLastMessageReplyMarkup = false,
                replyMarkup = if (showCancelButton) {
                    InlineKeyboardMarkup.createSingleButton(createCancelAppointmentButton(appointment))
                } else {
                    null
                }
            )
        }
    }

    private fun createCancelAppointmentButton(appointment: Appointment): InlineKeyboardButton {
        val data = CancelAppointmentCallbackHandler.convertToCallbackData(appointment.id)
        return InlineKeyboardButton.CallbackData(
            text = stringResources.getCancelAppointmentButtonText(),
            callbackData = data
        )
    }

    private fun createCancelAppointmentConfirmButton(appointment: Appointment): InlineKeyboardButton {
        val data = CancelConfirmedAppointmentCallbackHandler.convertToCallbackData(appointment.id)
        return InlineKeyboardButton.CallbackData(
            text = stringResources.getCancelAppointmentConfirmButtonText(),
            callbackData = data
        )
    }

    private fun askAboutAppointmentCancellation(bot: Bot, tgUser: User, chatId: Long, callbackData: String) {
        val appointmentId = CancelAppointmentCallbackHandler.convertFromCallbackData(callbackData)
        val appointment = appointmentService.getById(appointmentId)
        if (appointment == null) {
            sendPersistentMessage(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = stringResources.getAppointmentNotFoundMessage(),
                clearLastMessageReplyMarkup = true
            )
            return
        }
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
            clearLastMessageReplyMarkup = false,
            replyMarkup = InlineKeyboardMarkup.createSingleButton(createCancelAppointmentConfirmButton(appointment))
        )
    }

    private fun cancelAppointment(bot: Bot, tgUser: User, chatId: Long, callbackData: String) {
        val appointmentId = CancelConfirmedAppointmentCallbackHandler.convertFromCallbackData(callbackData)
        val appointment = appointmentService.getById(appointmentId)
        if (appointment == null) {
            sendPersistentMessage(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = stringResources.getAppointmentNotFoundMessage(),
                clearLastMessageReplyMarkup = true
            )
            return
        }
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
            text = text,
            clearLastMessageReplyMarkup = true,
        )
        val user = userService.getOrCreate(tgUser, chatId)
        sendAppointmentCancelledAdminNotification(bot, serviceName, day, time, user)
    }

    private fun handleServiceChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenService = ChooseServiceCallbackHandler(serviceService).convertFromCallbackData(callbackQuery.data)
        handleServiceChosen(bot, callbackQuery, chosenService)
    }

    private fun handleServiceChosen(bot: Bot, callbackQuery: CallbackQuery, service: Service) {
        val chatId = callbackQuery.message!!.chat.id
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
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
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
                replyMarkup = InlineKeyboardMarkup.create(withBackButton),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
            )
        }
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenDate = ChooseDateCallbackHandler.convertFromCallbackData(callbackQuery.data)
        handleDayChosen(bot, callbackQuery, chosenDate)
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery, day: LocalDate) {
        val chatId = callbackQuery.message!!.chat.id
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        val state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("handleDayChosen: State is null!")
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
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
                replyMarkup = InlineKeyboardMarkup.create(withBackButton),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
            )
            return
        }

        stateService.handleDayChosen(state, day)

        val list = mutableListOf<InlineKeyboardButton>()
        for (slot in slots) {
            val callbackData = ChooseTimeCallbackHandler.convertToCallbackData(slot)
            val button = InlineKeyboardButton.CallbackData(
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
            replyMarkup = InlineKeyboardMarkup.create(withBackButton),
            clearLastMessageReplyMarkup = true,
            keepMessage = false
        )
    }

    private fun createStubButton(): InlineKeyboardButton {
        return InlineKeyboardButton.CallbackData(text = " ", callbackData = createIgnoredCallbackData())
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenTime = ChooseTimeCallbackHandler.convertFromCallbackData(callbackQuery.data)
        handleTimeChosen(bot, callbackQuery, chosenTime)
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery, time: LocalTime) {
        val chatId = callbackQuery.message!!.chat.id
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        var state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("handleTimeChosen: State is null!")
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
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
                text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
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
            replyMarkup = InlineKeyboardMarkup.create(withBackButton),
            clearLastMessageReplyMarkup = true,
            keepMessage = false
        )
    }

    private fun createChooseServiceKeyboard(): ReplyMarkup {
        val buttons = serviceService.getAll().map {
            val callbackData = ChooseServiceCallbackHandler(serviceService).convertToCallbackData(it)
            InlineKeyboardButton.CallbackData(text = it.getDisplayName(), callbackData = callbackData)
        }
        return InlineKeyboardMarkup.create(buttons.map { listOf(it) })
    }

    private fun createChooseDayKeyboard(requiredDuration: Int): List<InlineKeyboardButton> {
        val daysAvailable = 28
        val list = mutableListOf<InlineKeyboardButton>()
        timeManagementService.getDaysThatHaveFreeWindows(daysAvailable, requiredDuration, getUserCurrentTime())
            .forEach { current ->
                val callbackData = ChooseDateCallbackHandler.convertToCallbackData(current)
                val button = InlineKeyboardButton.CallbackData(
                    text = VISIBLE_DATE_FORMATTER.format(current),
                    callbackData = callbackData
                )
                list.add(button)
            }
        return list
    }

    private fun createConfirmKeyboard(): List<List<InlineKeyboardButton>> {
        val confirmButton = InlineKeyboardButton.CallbackData(
            text = stringResources.getConfirmButtonText(),
            callbackData = CALLBACK_DATA_CONFIRM
        )
        val resetButton = createGoToBeginningButton()
        return listOf(listOf(resetButton, confirmButton))
    }

    private fun handleConfirm(bot: Bot, callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message!!.chat.id
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        var state = stateService.getUserState(user)
        if (state == null || !stateService.isFilled(state)) {
            stateService.clearState(user)
            sendReplaceableMessage(
                bot = bot,
                tgUser = callbackQuery.from,
                chatId = chatId,
                text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
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
                text = stringResources.getChosenTimeIsInThePastMessage(),
                clearLastMessageReplyMarkup = true
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
                clearLastMessageReplyMarkup = true,
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
                text = stringResources.getChosenTimeIsAlreadyTakenMessage(VISIBLE_TIME_FORMATTER.format(startTime)),
                clearLastMessageReplyMarkup = true
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
                text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                clearLastMessageReplyMarkup = true,
                keepMessage = false
            )
        }
    }

    private fun requestPhoneNumber(bot: Bot, tgUser: User, chatId: Long) {
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getRequestPhoneNumberMessage(),
            clearLastMessageReplyMarkup = false,
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
            replyMarkup = ReplyKeyboardRemove(),
            clearLastMessageReplyMarkup = true,
            keepMessage = true
        )
        sendPhoneSharedAdminNotification(bot, user)
    }

    private fun createBackButton(back: Back): List<InlineKeyboardButton> {
        val callback = BackCallbackHandler.convertToCallbackData(back)
        return listOf(
            InlineKeyboardButton.CallbackData(
                text = stringResources.getBackButtonText(),
                callbackData = callback
            )
        )
    }

    private fun goBack(bot: Bot, callbackQuery: CallbackQuery) {
        val back = BackCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val chatId = callbackQuery.message!!.chat.id
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
                        text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                        replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                        clearLastMessageReplyMarkup = true,
                        keepMessage = false
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
                        text = stringResources.getErrorOccuredWhileMakingAppointmentMessage(),
                        replyMarkup = InlineKeyboardMarkup.createSingleButton(createGoToBeginningButton()),
                        clearLastMessageReplyMarkup = true,
                        keepMessage = false
                    )
                } else {
                    handleDayChosen(bot, callbackQuery, day)
                }
            }
        }
    }

    private fun resetEverything(bot: Bot, callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message!!.chat.id
        val user = userService.getOrCreate(callbackQuery.from, chatId)
        stateService.clearState(user)
        suggestServicesForAppointment(bot, callbackQuery.from, chatId)
    }

    private fun createGoToBeginningButton(): InlineKeyboardButton {
        return InlineKeyboardButton.CallbackData(
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
        if (isAdmin(user.id)) {
            return
        }

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
                chatId = ChatId.fromId(admin.chatId),
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
        if (isAdmin(user.id)) {
            return
        }

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
                chatId = ChatId.fromId(admin.chatId),
                text = text
            )
        }
    }

    private fun sendPhoneSharedAdminNotification(
        bot: Bot,
        user: ru.round.shave.entity.User
    ) {
        if (isAdmin(user.id)) {
            return
        }

        val admins = getAdminUsers()
        if (admins.isEmpty()) {
            LOGGER.warn("No admins found!")
            return
        }
        val text = stringResources.getPhoneSharedAdminMessage(user = user)
        for (admin in admins) {
            bot.sendMessage(
                chatId = ChatId.fromId(admin.chatId),
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
        clearLastMessageReplyMarkup: Boolean,
        keepMessage: Boolean, // pass true if you want to prevent replacing this message in future
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null
    ) {
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Send replaceable message! User = ${user.getLogInfo()}, text = $text, keepMessage=$keepMessage")

        if (user.replaceableMessageId == null) {
            if (clearLastMessageReplyMarkup) {
                clearLastMessageReplyMarkup(bot, tgUser, chatId)
            }
            val msgId = sendNewMessageInternal(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = text,
                replyMarkup = replyMarkup,
                parseMode = parseMode,
                disableWebPagePreview = disableWebPagePreview
            )
            if (msgId != null && !keepMessage) {
                updateReplaceableMessageId(tgUser, chatId, msgId)
            }
            return
        }

        val res = bot.editMessageText(
            chatId = ChatId.fromId(user.chatId),
            messageId = user.replaceableMessageId,
            text = text,
            replyMarkup = replyMarkup
        )

        if (res.first?.isSuccessful != true) {
            if (clearLastMessageReplyMarkup) {
                clearLastMessageReplyMarkup(bot, tgUser, chatId)
            }
            val msgId = sendNewMessageInternal(
                bot = bot,
                tgUser = tgUser,
                chatId = chatId,
                text = text,
                replyMarkup = replyMarkup,
                parseMode = parseMode,
                disableWebPagePreview = disableWebPagePreview
            )
            if (msgId != null && !keepMessage) {
                updateReplaceableMessageId(tgUser, chatId, msgId)
            }
        }

        if (keepMessage) {
            updateReplaceableMessageId(tgUser, chatId, null)
        }
    }

    private fun updateReplaceableMessageId(tgUser: User, chatId: Long, replaceableMessageId: Long?) {
        val user = userService.getOrCreate(tgUser, chatId)
        userService.update(user.copy(replaceableMessageId = replaceableMessageId))
    }

    private fun sendPersistentMessage(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        text: String,
        clearLastMessageReplyMarkup: Boolean,
        replyMarkup: ReplyMarkup? = null,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null
    ) {
        if (clearLastMessageReplyMarkup) {
            clearLastMessageReplyMarkup(bot, tgUser, chatId)
        }
        sendNewMessageInternal(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = text,
            replyMarkup = replyMarkup,
            parseMode = parseMode,
            disableWebPagePreview = disableWebPagePreview
        )
        // Reset replaceableMessageId to continue conversation below current message
        val user = userService.getOrCreate(tgUser, chatId)
        LOGGER.info("Send persistent message! User = ${user.getLogInfo()}, text = $text")
        userService.update(user.copy(replaceableMessageId = null))
    }

    private fun sendNewMessageInternal(
        bot: Bot,
        tgUser: User,
        chatId: Long,
        text: String,
        replyMarkup: ReplyMarkup?,
        parseMode: ParseMode?,
        disableWebPagePreview: Boolean?
    ): Long? {
        val response = bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = text,
            parseMode = parseMode,
            replyMarkup = replyMarkup,
            disableWebPagePreview = disableWebPagePreview
        )
        val messageId = response.first?.body()?.result?.messageId
        if (messageId != null) {
            val user = userService.getOrCreate(tgUser, chatId)
            userService.update(user.copy(lastMessageId = messageId))
        }
        return messageId
    }

    private fun clearLastMessageReplyMarkup(bot: Bot, tgUser: User, chatId: Long) {
        val user = userService.getOrCreate(tgUser, chatId)
        if (user.lastMessageId != null) {
            bot.editMessageReplyMarkup(
                chatId = ChatId.fromId(chatId),
                messageId = user.lastMessageId,
                replyMarkup = null
            )
        }
    }

    fun sendMessageByChatId(chatId: Long, text: String) {
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = text
        )
    }

    fun sendLocation(chatId: Long) {
        LOGGER.info("Sending location to chat $chatId")
        bot.sendLocation(
            chatId = ChatId.fromId(chatId),
            latitude = LATITUDE,
            longitude = LONGITUDE
        )
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

    private fun isAdmin(userId: Long): Boolean {
        return ADMIN_USER_IDS.contains(userId)
    }

    private fun sendErrorToDeveloper(bot: Bot, error: String) {
        val developer = userService.getById(DEVELOPER_USER_ID) ?: return
        bot.sendMessage(
            chatId = ChatId.fromId(developer.chatId),
            text = "Achtung! $error"
        )
    }

    private fun sendToDeveloper(bot: Bot, message: String) {
        val developer = userService.getById(DEVELOPER_USER_ID) ?: return
        bot.sendMessage(
            chatId = ChatId.fromId(developer.chatId),
            text = message
        )
    }

    private fun sendMessageWithMasterContacts(bot: Bot, tgUser: User, chatId: Long) {
        val locationButton = InlineKeyboardButton.CallbackData(
            text = stringResources.getSendLocationButtonText(),
            callbackData = CALLBACK_DATA_SEND_LOCATION
        )
        val replyMarkup = InlineKeyboardMarkup.createSingleButton(locationButton)
        sendPersistentMessage(
            bot = bot,
            tgUser = tgUser,
            chatId = chatId,
            text = stringResources.getMasterContacts(),
            clearLastMessageReplyMarkup = true,
            replyMarkup = replyMarkup,
            disableWebPagePreview = true
        )
    }

    private fun handleAdminHasChosenDayForFreeWindows(bot: Bot, callbackQuery: CallbackQuery) {
        val chatId = callbackQuery.message!!.chat.id
        val chosenDate = ChooseDateForFreeWindowsCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val dateStr = VISIBLE_DATE_FORMATTER_FULL.format(chosenDate)
        val windows = timeManagementService.getAllFreeWindows(chosenDate)
        val sb = StringBuilder(stringResources.getFreeWindowsForDayText(dateStr))
        sb.append('\n').append('\n')
        if (windows.isEmpty()) {
            sb.append(stringResources.getNoFreeWindowsFoundAdminMessage())
        } else {
            for (window in windows) {
                val start = VISIBLE_TIME_FORMATTER.format(window.start)
                val end = VISIBLE_TIME_FORMATTER.format(window.end)
                val line = "$start — $end"
                sb.append(line)
                sb.append('\n')
            }
        }

        sendReplaceableMessage(
            bot = bot,
            tgUser = callbackQuery.from,
            chatId = chatId,
            text = sb.toString(),
            clearLastMessageReplyMarkup = false,
            keepMessage = false
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RoundBot::class.java.simpleName)
        private const val TOKEN_ENVIRONMENT_VARIABLE = "ROUND_SHAVE_TOKEN"
        val TIME_ZONE: ZoneId = ZoneId.of("Europe/Moscow")

        private val VISIBLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM")
        private val VISIBLE_DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val VISIBLE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private const val CALLBACK_DATA_RESET = "callback_data_reset"
        private const val CALLBACK_DATA_CONFIRM = "callback_data_confirm"
        private const val CALLBACK_DATA_APPOINTMENTS_IN_PAST = "appointments_in_past"
        private const val CALLBACK_DATA_APPOINTMENTS_IN_FUTURE = "appointments_in_future"
        private const val CALLBACK_DATA_MY_FREE_WINDOWS = "free_admin_windows"
        private const val CALLBACK_DATA_SEND_LOCATION = "callback_data_send_location"

        private const val DAYS_PER_ROW = 4

        private const val DEVELOPER_USER_ID = 225893185L
        private val ADMIN_USER_IDS = arrayOf(DEVELOPER_USER_ID, 1661635722L)

        private const val LATITUDE = 55.762316f
        private const val LONGITUDE = 37.530532f
    }
}