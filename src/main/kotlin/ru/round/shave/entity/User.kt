package ru.round.shave.entity

data class User(
    val id: Long,
    val isBot: Boolean,
    val firstName: String,
    val lastName: String?,
    val username: String?,
    val languageCode: String?,
    val canJoinGroups: Boolean?,
    val canReadAllGroupMessages: Boolean?,
    val supportsInlineQueries: Boolean?
) {

    companion object {
        fun from(user: com.github.kotlintelegrambot.entities.User): User {
            return User(
                id = user.id,
                isBot = user.isBot,
                firstName = user.firstName,
                lastName = user.lastName,
                username = user.username,
                languageCode = user.languageCode,
                canJoinGroups = user.canJoinGroups,
                canReadAllGroupMessages = user.canReadAllGroupMessages,
                supportsInlineQueries = user.supportsInlineQueries,
            )
        }
    }
}