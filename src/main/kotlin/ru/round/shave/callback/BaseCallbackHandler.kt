package ru.round.shave.callback

abstract class BaseCallbackHandler<T>(private val prefix: String) : CallbackHandler<T> {

    override fun canHandle(callbackData: String): Boolean {
        return callbackData.startsWith(prefix)
    }

    override fun convertToCallbackData(data: T): String {
        return prefix + encodeData(data)
    }

    override fun convertFromCallbackData(callbackData: String): T {
        if (!callbackData.startsWith(prefix)) {
            throw IllegalArgumentException("Callback data must starts with $prefix, actual: $callbackData")
        }
        val withoutPrefix = callbackData.drop(prefix.length)
        if (withoutPrefix.isEmpty()) {
            throw IllegalArgumentException("Callback data without prefix is empty")
        }
        return decodeData(withoutPrefix)
    }

    protected abstract fun encodeData(data: T): String

    @Throws(IllegalArgumentException::class)
    protected abstract fun decodeData(value: String): T
}