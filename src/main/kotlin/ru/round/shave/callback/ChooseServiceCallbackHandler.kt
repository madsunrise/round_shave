package ru.round.shave.callback

import ru.round.shave.entity.Service
import ru.round.shave.service.ServiceService

class ChooseServiceCallbackHandler(private val serviceService: ServiceService) :
    BaseCallbackHandler<Service>(Prefixes.CHOOSE_SERVICE_PREFIX) {

    override fun encodeData(data: Service): String {
        return data.id.toString()
    }

    override fun decodeData(value: String): Service {
        return serviceService.getById(value.toLong())!!
    }
}