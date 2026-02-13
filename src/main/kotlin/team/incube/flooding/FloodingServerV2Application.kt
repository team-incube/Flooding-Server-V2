package team.incube.flooding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FloodingServerV2Application

fun main(args: Array<String>) {
    runApplication<FloodingServerV2Application>(*args)
}
