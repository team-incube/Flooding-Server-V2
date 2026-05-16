package team.incube.flooding.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import team.incube.flooding.global.config.FileStorageConstants.IMAGE_RESOURCE_SUB_DIRS
import team.incube.flooding.global.config.FileStorageConstants.IMAGE_URL_PREFIX
import java.nio.file.Paths

@Configuration
class WebMvcConfig(
    private val fileStorageProperties: FileStorageProperties,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadRoot =
            Paths
                .get(fileStorageProperties.uploadDir)
                .toAbsolutePath()
                .normalize()

        IMAGE_RESOURCE_SUB_DIRS.forEach { subDir ->
            registry
                .addResourceHandler("$IMAGE_URL_PREFIX/$subDir/**")
                .addResourceLocations(
                    uploadRoot
                        .resolve(subDir)
                        .normalize()
                        .toUri()
                        .toString()
                        .let { if (it.endsWith("/")) it else "$it/" },
                )
        }
    }
}
