package team.incube.flooding.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebMvcConfig(
    private val fileStorageProperties: FileStorageProperties,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = Paths.get(fileStorageProperties.uploadDir).toAbsolutePath().normalize()

        registry
            .addResourceHandler("/images/**")
            .addResourceLocations(uploadPath.toUri().toString())
    }
}
