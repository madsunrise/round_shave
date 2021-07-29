package ru.round.shave

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.round.shave.callback.ChooseDateCallbackHandler
import ru.round.shave.callback.ChooseServiceCallbackHandler
import ru.round.shave.callback.ChooseTimeCallbackHandler
import ru.round.shave.entity.Appointment
import ru.round.shave.service.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    @PostConstruct
    fun run() {
        val bot = bot {
            token = TOKEN
            dispatch {
                command("start") {
                    val chatId = ChatId.fromId(message.chat.id)
                    bot.sendMessage(chatId, "Вас приветствует Round Shave Bot!")
                    sendInitialMessage(bot, chatId)
                }

                callbackQuery(data = CALLBACK_DATA_RESET) {
                    val chatId = ChatId.fromId(this.callbackQuery.message!!.chat.id)
                    sendInitialMessage(bot, chatId)
                }

                callbackQuery(data = CALLBACK_DATA_CONFIRM) {
                    handleConfirm(bot, callbackQuery)
                }

                callbackQuery {
                    val callbackData = this.callbackQuery.data
                    when {
                        ChooseServiceCallbackHandler(serviceService).canHandle(callbackData) -> {
                            handleServiceChosen(bot, this.callbackQuery)
                        }
                        ChooseDateCallbackHandler.canHandle(callbackData) -> {
                            handleDayChosen(bot, this.callbackQuery)
                        }
                        ChooseTimeCallbackHandler.canHandle(callbackData) -> {
                            handleTimeChosen(bot, this.callbackQuery)
                        }
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun sendInitialMessage(bot: Bot, chatId: ChatId) {
        bot.sendMessage(
            chatId,
            "Выберите тип услуги.",
            replyMarkup = createChooseServiceKeyboard()
        )
    }

    private fun handleServiceChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenService = ChooseServiceCallbackHandler(serviceService).convertFromCallbackData(callbackQuery.data)
        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from)
        stateService.clearState(user)

        val freeDaysButtons = createChooseDayKeyboard(chosenService.duration)
        if (freeDaysButtons.isEmpty()) {
            bot.sendMessage(
                chatId,
                "К сожалению, запись временно недоступна."
            )
        } else {
            val state = stateService.createNewState(user)
            stateService.handleServiceChosen(state, chosenService)
            bot.sendMessage(
                chatId,
                listOf(
                    "Выбранная услуга: ${chosenService.getDisplayName()}",
                    "Выберите день посещения."
                ).joinToString(separator = "\n"),
                replyMarkup = InlineKeyboardMarkup.create(freeDaysButtons.chunked(DAYS_PER_ROW))
            )
        }
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenDate = ChooseDateCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from)
        val state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("State is null!")
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
            return
        }

        val duration = state.service!!.duration
        val slots = timeManagementService.getFreeWindows(chosenDate, duration)
        if (slots.isEmpty()) {
            val freeDaysButtons = createChooseDayKeyboard(duration)
            bot.sendMessage(
                chatId,
                "К сожалению, запись на ${VISIBLE_DATE_FORMATTER_FULL.format(chosenDate)} недоступна. Выберите другую дату.",
                replyMarkup = InlineKeyboardMarkup.create(freeDaysButtons.chunked(DAYS_PER_ROW))
            )
            return
        }

        stateService.handleDayChosen(state, chosenDate)

        val list = mutableListOf<InlineKeyboardButton.CallbackData>()
        for (slot in slots) {
            val callbackData = ChooseTimeCallbackHandler.convertToCallbackData(slot)
            val button = InlineKeyboardButton.CallbackData(VISIBLE_TIME_FORMATTER.format(slot), callbackData)
            list.add(button)
        }

        bot.sendMessage(
            chatId,
            listOf(
                state.service.getDisplayName(),
                "Выбранный день: ${VISIBLE_DATE_FORMATTER_FULL.format(chosenDate)}",
                "Выберите время посещения."
            ).joinToString(separator = "\n"),
            replyMarkup = InlineKeyboardMarkup.create(list.chunked(4))
        )

    }

    private fun createChooseServiceKeyboard(): ReplyMarkup {
        val buttons = serviceService.getAll().map {
            val callbackData = ChooseServiceCallbackHandler(serviceService).convertToCallbackData(it)
            InlineKeyboardButton.CallbackData(it.getDisplayNameWithPrice(), callbackData)
        }
        return InlineKeyboardMarkup.create(buttons.chunked(1))
    }

    private fun createChooseDayKeyboard(requiredDuration: Int): List<InlineKeyboardButton.CallbackData> {
        val daysAvailable = 12
        val list = mutableListOf<InlineKeyboardButton.CallbackData>()
        timeManagementService.getDaysThatHaveFreeWindows(daysAvailable, requiredDuration).forEach { current ->
            val callbackData = ChooseDateCallbackHandler.convertToCallbackData(current)
            val button = InlineKeyboardButton.CallbackData(VISIBLE_DATE_FORMATTER.format(current), callbackData)
            list.add(button)
        }
        return list
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenTime = ChooseTimeCallbackHandler.convertFromCallbackData(callbackQuery.data)

        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = userService.getOrCreate(callbackQuery.from)
        var state = stateService.getUserState(user)

        if (state == null) {
            LOGGER.warn("State is null!")
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
            return
        }

        state = stateService.handleTimeChosen(state, chosenTime)

        if (!stateService.isFilled(state)) {
            LOGGER.warn("State is not filled!")
            stateService.clearState(user)
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = listOf(
                state.service!!.getDisplayName(),
                "Дата и время: ${state.day!!.format(VISIBLE_DATE_FORMATTER_FULL)} ${
                    state.time!!.format(VISIBLE_TIME_FORMATTER)
                }",
                "Стоимость: ${state.service!!.getDisplayPrice()}",
                "",
                "Всё верно?",
            ).joinToString(separator = "\n"),
            replyMarkup = createConfirmKeyboard()
        )
    }

    private fun createConfirmKeyboard(): ReplyMarkup {
        val confirmButton = InlineKeyboardButton.CallbackData("Записаться", CALLBACK_DATA_CONFIRM)
        val resetButton = InlineKeyboardButton.CallbackData("В начало", CALLBACK_DATA_RESET)
        return InlineKeyboardMarkup.createSingleRowKeyboard(confirmButton, resetButton)
    }

    private fun handleConfirm(bot: Bot, callbackQuery: CallbackQuery) {
        val user = userService.getOrCreate(callbackQuery.from)
        val state = stateService.getUserState(user)
        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        if (state == null || !stateService.isFilled(state)) {
            stateService.clearState(user)
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
            return
        }
        LOGGER.info("Confirmed state for user ${user.id}: $state")
        val service = state.service!!
        val startTime = LocalDateTime.of(state.day, state.time)

        val appointment = Appointment(
            user = user,
            services = listOf(service),
            startTime = startTime,
            endTime = startTime.plusMinutes(service.duration.toLong())
        )
        try {
            appointmentService.insert(appointment)
            bot.sendMessage(
                chatId,
                listOf(
                    "Запись подтверждена. Ждём вас " +
                            "${VISIBLE_DATE_FORMATTER_FULL.format(state.day)} в " +
                            "${VISIBLE_TIME_FORMATTER.format(startTime)} по адресу: 2-я Магистральная ул., 3с3.",
                    "",
                    service.getDisplayName()
                ).joinToString(separator = "\n")
            )
        } catch (e: Exception) {
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
        } finally {
            stateService.clearState(user)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RoundBot::class.java.simpleName)
        private const val TOKEN = "1877093277:AAEG_rkWcXzVp9jqmDjwmK6Fi5cmHY4JZyI"
        val TIME_ZONE: ZoneId = ZoneId.of("Europe/Moscow")

        private val VISIBLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM")
        private val VISIBLE_DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val VISIBLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH.mm")

        private const val CALLBACK_DATA_RESET = "reset"
        private const val CALLBACK_DATA_CONFIRM = "confirm"

        private const val DAYS_PER_ROW = 4
    }
}