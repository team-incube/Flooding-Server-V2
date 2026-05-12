package team.incube.flooding.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import team.incube.flooding.global.config.FileStorageConstants.CLUB_IMAGE_RESOURCE_PATTERN
import team.incube.flooding.global.config.FileStorageConstants.CLUB_IMAGE_SUB_DIR
import java.nio.file.Paths

@Configuration
class WebMvcConfig(
    private val fileStorageProperties: FileStorageProperties,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val clubUploadPath =
            Paths
                .get(fileStorageProperties.uploadDir)
                .toAbsolutePath()
                .normalize()
                .resolve(CLUB_IMAGE_SUB_DIR)
                .normalize()

        registry
            .addResourceHandler(CLUB_IMAGE_RESOURCE_PATTERN)
            .addResourceLocations(
                clubUploadPath
                    .toUri()
                    .toString()
                    .let { if (it.endsWith("/")) it else "$it/" },
            )
    }
}
