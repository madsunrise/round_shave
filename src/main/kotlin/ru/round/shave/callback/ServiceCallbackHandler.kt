package ru.round.shave.callback

import ru.round.shave.entity.Service

object ServiceCallbackHandler: BaseCallbackHandler<Service>() {

    override val prefix: String = Prefixes.CHOOSE_SERVICE_PREFIX

    override fun encodeData(data: Service): String {
        return data.name
    }

    override fun decodeData(value: String): Service {
        return Service.valueOf(value)
    }
}