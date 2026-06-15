package team.incube.flooding.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {
    companion object {
        val postgres =
            PostgreSQLContainer<Nothing>("postgres:16").apply {
                start()
            }

        val redis =
            GenericContainer<Nothing>("redis:7").apply {
                withExposedPorts(6379)
                start()
            }

        @DynamicPropertySource
        @JvmStatic
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
