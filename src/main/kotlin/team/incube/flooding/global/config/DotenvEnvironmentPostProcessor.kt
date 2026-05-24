package team.incube.flooding.global.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.io.File

class DotenvEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val envFile = File(".env")
        if (!envFile.exists()) return

        val properties =
            envFile
                .readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
                .associate { line ->
                    val (key, value) = line.split("=", limit = 2)
                    key.trim() to value.trim()
                }

        environment.propertySources.addLast(MapPropertySource("dotenv", properties))
    }
}
