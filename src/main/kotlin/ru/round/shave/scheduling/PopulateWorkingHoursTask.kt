package ru.round.shave.scheduling

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.round.shave.entity.WorkingHours
import ru.round.shave.service.WorkingHoursService
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Component
class PopulateWorkingHoursTask {

    @Autowired
    private lateinit var workingHoursService: WorkingHoursService

    private val startHour = LocalTime.of(10, 0)
    private val endHour = LocalTime.of(22, 0)

    @Scheduled(cron = Cron.POPULATE_WORKING_HOURS)
    fun run() {
        populate()
        clearOld()
    }

    private fun populate() {
        LOGGER.info("Populating working hours")
        var current = LocalDate.now()
        while (current.dayOfWeek != DayOfWeek.MONDAY) {
            current = current.plusDays(1L)
        }
        while (current.dayOfWeek != DayOfWeek.SUNDAY) { // Sunday is a day off
            val entity = WorkingHours(
                day = current,
                startTime = startHour,
                endTime = endHour
            )
            workingHoursService.insert(entity)
            LOGGER.info("Added: $current")
            current = current.plusDays(1L)
        }
    }

    private fun clearOld() {
        // we can take LocalDate.now(), but we subtract 1 day to be 100% sure
        val clearBefore = LocalDate.now().minusDays(1L)
        LOGGER.info("Clearing old records")
        val cleared = workingHoursService.deleteAllBefore(clearBefore)
        LOGGER.info("$cleared records have been deleted")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PopulateWorkingHoursTask::class.java.simpleName)
    }
}