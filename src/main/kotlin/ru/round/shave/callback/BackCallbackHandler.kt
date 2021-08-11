package ru.round.shave.callback

import ru.round.shave.entity.Back

object BackCallbackHandler : BaseCallbackHandler<Back>(Prefixes.BACK_PREFIX) {

    override fun encodeData(data: Back): String {
        return data.name
    }

    override fun decodeData(value: String): Back {
        return Back.valueOf(value)
    }
}
