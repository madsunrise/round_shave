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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.round.shave.callback.ChooseDateCallbackHandler
import ru.round.shave.callback.ChooseServiceCallbackHandler
import ru.round.shave.callback.ChooseTimeCallbackHandler
import ru.round.shave.entity.Service
import ru.round.shave.entity.User
import ru.round.shave.service.StateService
import ru.round.shave.service.TimeService
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@Component
class RoundBot {

    @Autowired
    private lateinit var stateService: StateService

    @Autowired
    private lateinit var timeService: TimeService

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
                    val user = User.from(callbackQuery.from)
                    val state = stateService.getState(user)
                    val chatId = ChatId.fromId(this.callbackQuery.message!!.chat.id)
                    if (state == null || !state.isFull()) {
                        bot.sendMessage(
                            chatId,
                            "К сожалению, произошла ошибка. Повторите процедуру записи."
                        )
                        return@callbackQuery
                    }
                    println("Confirmed state: $state")
                    bot.sendMessage(
                        chatId,
                        "Запись подтверждена. Ждём вас " +
                                "${VISIBLE_DATE_FORMATTER_FULL.format(state.day)} в " +
                                "${VISIBLE_TIME_FORMATTER.format(state.time)} по адресу: 2-я Магистральная ул., 3с3."
                    )
                }

                callbackQuery {
                    val callbackData = this.callbackQuery.data
                    when {
                        ChooseServiceCallbackHandler.canHandle(callbackData) -> {
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
        val chosenService = ChooseServiceCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = User.from(callbackQuery.from)
        stateService.saveSelectedService(user, chosenService)
        bot.sendMessage(
            chatId,
            "Выберите день посещения.",
            replyMarkup = createChooseDayKeyboard()
        )
    }

    private fun handleDayChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenDate = ChooseDateCallbackHandler.convertFromCallbackData(callbackQuery.data)
        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val slots = timeService.getFreeSlots(chosenDate)
        if (slots.isEmpty()) {
            bot.sendMessage(
                chatId,
                "К сожалению, запись на ${VISIBLE_DATE_FORMATTER.format(chosenDate)} недоступна. Выберите другую дату.",
                replyMarkup = createChooseDayKeyboard()
            )
            return
        }

        val user = User.from(callbackQuery.from)
        stateService.saveSelectedDay(user, chosenDate)

        val list = mutableListOf<InlineKeyboardButton.CallbackData>()
        for (slot in slots) {
            val callbackData = ChooseTimeCallbackHandler.convertToCallbackData(slot)
            val button = InlineKeyboardButton.CallbackData(VISIBLE_TIME_FORMATTER.format(slot), callbackData)
            list.add(button)
        }

        bot.sendMessage(
            chatId,
            "Выберите время посещения.",
            replyMarkup = InlineKeyboardMarkup.create(list.chunked(4))
        )

    }

    private fun createChooseServiceKeyboard(): ReplyMarkup {
        val buttons = Service.values().map {
            val callbackData = ChooseServiceCallbackHandler.convertToCallbackData(it)
            InlineKeyboardButton.CallbackData(it.userVisibleName, callbackData)
        }
        return InlineKeyboardMarkup.create(buttons.chunked(2))
    }

    private fun createChooseDayKeyboard(): ReplyMarkup {
        val daysAvailable = 12
        val list = mutableListOf<InlineKeyboardButton.CallbackData>()

        timeService.getDaysThatHaveFreeSlots(daysAvailable).forEach { current ->
            val callbackData = ChooseDateCallbackHandler.convertToCallbackData(current)
            val button = InlineKeyboardButton.CallbackData(VISIBLE_DATE_FORMATTER.format(current), callbackData)
            list.add(button)
        }
        return InlineKeyboardMarkup.create(list.chunked(4))
    }

    private fun handleTimeChosen(bot: Bot, callbackQuery: CallbackQuery) {
        val chosenTime = ChooseTimeCallbackHandler.convertFromCallbackData(callbackQuery.data)

        val chatId = ChatId.fromId(callbackQuery.message!!.chat.id)
        val user = User.from(callbackQuery.from)
        stateService.saveSelectedTime(user, chosenTime)

        val currentState = stateService.getState(user)
        if (currentState == null || !currentState.isFull()) {
            bot.sendMessage(
                chatId,
                "К сожалению, произошла ошибка. Повторите процедуру записи."
            )
            return
        }


        bot.sendMessage(
            chatId = chatId,
            text = listOf(
                "Выбранная услуга: ${currentState.service!!.userVisibleName}",
                "Дата и время: ${currentState.day!!.format(VISIBLE_DATE_FORMATTER_FULL)} ${
                    currentState.time!!.format(VISIBLE_TIME_FORMATTER)
                }",
                "Всё правильно?",
            ).joinToString(separator = "\n"),
            replyMarkup = createConfirmKeyboard()
        )
    }

    private fun createConfirmKeyboard(): ReplyMarkup {
        val confirmButton = InlineKeyboardButton.CallbackData("Записаться", CALLBACK_DATA_CONFIRM)
        val resetButton = InlineKeyboardButton.CallbackData("В начало", CALLBACK_DATA_RESET)
        return InlineKeyboardMarkup.createSingleRowKeyboard(confirmButton, resetButton)
    }

    companion object {
        private const val TOKEN = "1877093277:AAEG_rkWcXzVp9jqmDjwmK6Fi5cmHY4JZyI"
        val TIME_ZONE: ZoneId = ZoneId.of("Europe/Moscow")

        private val VISIBLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM")
        private val VISIBLE_DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val VISIBLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH.mm")

        private val CALLBACK_DATA_RESET = "reset"
        private val CALLBACK_DATA_CONFIRM = "confirm"
    }
}