package ru.round.shave.callback

interface CallbackHandler<T> {

    fun canHandle(callbackData: String): Boolean

    fun convertToCallbackData(data: T): String

    @Throws(IllegalArgumentException::class)
    fun convertFromCallbackData(callbackData: String): T
}