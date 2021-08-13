package ru.round.shave.scheduling

object Cron {
    const val POPULATE_WORKING_HOURS = "0 0 0 * * *" // Every day at 3:00 (MSK)
    const val REMINDER = "0 0/5 * * * *" // every 5 minutes
}
