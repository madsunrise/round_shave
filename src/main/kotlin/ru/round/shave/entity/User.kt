package ru.round.shave.entity

import javax.persistence.*

@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @Column(name = "is_bot", nullable = false)
    val isBot: Boolean,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = true)
    val lastName: String?,

    @Column(name = "username", nullable = true, unique = true)
    val username: String?,

    @Column(name = "language_code", nullable = true, length = 10)
    val languageCode: String?,

    @Column(name = "can_join_groups", nullable = true)
    val canJoinGroups: Boolean?,

    @Column(name = "can_read_all_group_messages", nullable = true)
    val canReadAllGroupMessages: Boolean?,

    @Column(name = "support_inline_queries", nullable = true)
    val supportsInlineQueries: Boolean?
) {

    constructor() : this(
        isBot = false,
        firstName = "",
        lastName = null,
        username = null,
        languageCode = null,
        canJoinGroups = null,
        canReadAllGroupMessages = null,
        supportsInlineQueries = null
    )

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