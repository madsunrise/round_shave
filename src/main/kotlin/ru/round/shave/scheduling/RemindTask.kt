package ru.round.shave.scheduling

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.round.shave.RoundBot
import ru.round.shave.service.AppointmentService
import ru.round.shave.strings.RussianStringResources
import ru.round.shave.strings.StringResources
import java.time.LocalDateTime

@Component
class RemindTask {

    @Autowired
    private lateinit var roundBot: RoundBot

    @Autowired
    private lateinit var appointmentService: AppointmentService

    private val stringResources: StringResources = RussianStringResources

    @Scheduled(cron = Cron.REMINDER)
    fun run() {
        LOGGER.info("Start checking for reminders")
        val remindBeforeInHours = 1L

        val now = LocalDateTime.now(RoundBot.TIME_ZONE).roundToMinute()
        val targetTime = now.plusHours(remindBeforeInHours)
        val appointment = appointmentService.getByStartTime(targetTime)
        if (appointment != null) {
            LOGGER.info("Found appointment at ${appointment.startTime} for user ${appointment.user.getLogInfo()}")
            val serviceName = appointment.services.first().getDisplayName()
            val time = appointment.startTime.format(RoundBot.VISIBLE_TIME_FORMATTER)
            val text = stringResources.getRemindBeforeTwoHoursText(time, serviceName)
            roundBot.sendMessageByChatId(appointment.user.chatId, text)
            roundBot.sendLocation(appointment.user.chatId)
        }
    }

    private fun LocalDateTime.roundToMinute(): LocalDateTime {
        val currentMinute = minute
        val currentSecond = second
        if (currentSecond in 0..29) {
            return floorToMinute()
        } else {
            val afterFloor = floorToMinute()
            if (afterFloor.minute == currentMinute) {
                return afterFloor.plusMinutes(1)
            }
            return afterFloor
        }
    }

    private fun LocalDateTime.floorToMinute(): LocalDateTime = withSecond(0).withNano(0)

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RemindTask::class.java.simpleName)
    }
}
