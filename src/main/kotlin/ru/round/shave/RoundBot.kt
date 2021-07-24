package ru.round.shave

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.springframework.stereotype.Component
import ru.round.shave.callback.ServiceCallbackHandler
import ru.round.shave.entity.Service
import ru.round.shave.entity.TimeRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct
import kotlin.random.Random

@Component
class RoundBot {

    @PostConstruct
    fun run() {
        val bot = bot {
            token = TOKEN
            dispatch {
                command("start") {
                    sendInitialMessage(bot, message)
                }

                callbackQuery {
                    val callbackData = this.callbackQuery.data
                    val chatId = ChatId.fromId(this.callbackQuery.message!!.chat.id)
                    if (ServiceCallbackHandler.canHandle(callbackData)) {
                        val chosenService = ServiceCallbackHandler.convertFromCallbackData(callbackData)
                        bot.sendMessage(chatId, "Выбрано: ${chosenService.userVisibleName}")
                        val userId = this.callbackQuery.from.id
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun sendInitialMessage(bot: Bot, message: Message) {
        val chatId = ChatId.fromId(message.chat.id)
        bot.sendMessage(
            chatId,
            "Привет! Выбери тип услуги.",
            replyMarkup = createChooseServiceKeyboard()
        )
    }

    private fun createChooseServiceKeyboard(): ReplyMarkup {
        val buttons = Service.values().map {
            val callbackData = ServiceCallbackHandler.convertToCallbackData(it)
            InlineKeyboardButton.CallbackData(it.userVisibleName, callbackData)
        }
        return InlineKeyboardMarkup.create(buttons.chunked(2))
    }

    private fun onUserHasChosenDate(date: LocalDate, bot: Bot, chat: Chat) {
        val freeSlots = getFreeSlots(date)
        val chatId = ChatId.fromId(chat.id)
        if (freeSlots.isEmpty()) {
            val formatter = DATE_FORMATTER_FULL
            bot.sendMessage(
                chatId = chatId,
                text = "К сожалению, запись на ${formatter.format(date)} недоступна."
            )
        }

        bot.sendMessage(
            chatId,
            "Дата: ${DATE_FORMATTER_FULL.format(date)}. Выбери время",
            replyMarkup = getKeyboardWithSlots(freeSlots)
        )
    }

    private fun getFreeSlots(date: LocalDate): List<TimeRange> {
        // query db
        val count = Random.Default.nextInt(9)
        val list = mutableListOf<TimeRange>()
        var hour = 10
        for (i in 0 until count) {
            val start = LocalDateTime.of(date, LocalTime.of(hour, 0))
            val end = start.plusHours(1L)
            list.add(TimeRange(start, end))
            hour++
        }
        return list
    }

    private fun getKeyboardWithSlots(slots: List<TimeRange>): ReplyMarkup {
        val buttons = slots
            .map { "${TIME_FORMATTER.format(it.start)} - ${TIME_FORMATTER.format(it.end)}" }
            .map { InlineKeyboardButton.CallbackData(it, "HZ CHTO TUT") }
            .chunked(3)
        return InlineKeyboardMarkup.create(buttons)
    }


    private fun createInitialKeyboard(): ReplyMarkup {
        var current = LocalDate.now(TIME_ZONE)
        val list = mutableListOf<InlineKeyboardButton.CallbackData>()
        for (i in 0 until DAYS_AVAILABLE) {
            val button =
                InlineKeyboardButton.CallbackData(DATE_FORMATTER_SHORT.format(current), CHOOSE_DATE_CALLBACK_DATA)
            list.add(button)
            current = current.plusDays(1L)
        }
        return InlineKeyboardMarkup.createSingleRowKeyboard(list)
    }


    companion object {
        private const val TOKEN = "1877093277:AAEG_rkWcXzVp9jqmDjwmK6Fi5cmHY4JZyI"
        private val TIME_ZONE = ZoneId.of("Europe/Moscow")

        private const val DAYS_AVAILABLE = 5

        private val DATE_FORMATTER_SHORT = DateTimeFormatter.ofPattern("dd.MM")
        private val DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

        private const val CHOOSE_SERVICE_CALLBACK_DATA = "choose_service"
        private const val CHOOSE_DATE_CALLBACK_DATA = "choose_date"
    }
}