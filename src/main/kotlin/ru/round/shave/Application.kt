package ru.round.shave

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
open class Application : SpringBootServletInitializer() {

    @PostConstruct
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    override fun configure(builder: SpringApplicationBuilder?): SpringApplicationBuilder? {
        return builder?.sources(Application::class.java)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}