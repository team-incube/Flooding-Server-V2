package team.incube.flooding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class FloodingServerV2Application

fun main(args: Array<String>) {
    runApplication<FloodingServerV2Application>(*args)
}
