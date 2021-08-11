package ru.round.shave.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "user")
data class User(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long = -1,

    @Column(name = "chat_id", nullable = false)
    val chatId: Long,

    @Column(name = "is_bot", nullable = false)
    val isBot: Boolean,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = true)
    val lastName: String?,

    @Column(name = "username", nullable = true)
    val username: String?,

    @Column(name = "phone", nullable = true, length = 16)
    val phone: String?,

    @Column(name = "replaceable_message_id", nullable = true)
    val replaceableMessageId: Long?,

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
        chatId = -1L,
        isBot = false,
        firstName = "",
        lastName = null,
        username = null,
        phone = null,
        replaceableMessageId = null,
        languageCode = null,
        canJoinGroups = null,
        canReadAllGroupMessages = null,
        supportsInlineQueries = null
    )

    override fun toString(): String {
        return "User(id=$id, chatId=$chatId, isBot=$isBot, firstName='$firstName', lastName=$lastName, " +
                "username=$username, phone=$phone, replaceableMessageId=$replaceableMessageId, languageCode=$languageCode)"
    }

    fun getLogInfo(): String {
        val sb = StringBuilder(firstName)
        if (!lastName.isNullOrBlank()) {
            sb.append(' ')
            sb.append(lastName)
        }

        sb.append(' ')
        sb.append(id)

        if (!username.isNullOrBlank()) {
            sb.append(" (@")
            sb.append(username)
            sb.append(')')
        }

        return sb.toString()
    }
}