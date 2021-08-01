package ru.round.shave.callback

import ru.round.shave.entity.Back

object BackCallbackHandler : BaseCallbackHandler<Back>() {

    override val prefix: String = Prefixes.BACK_PREFIX

    override fun encodeData(data: Back): String {
        return data.name
    }

    override fun decodeData(value: String): Back {
        return Back.valueOf(value)
    }
}
