package team.incube.flooding.domain.dormitory.massage.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalTime

@ConfigurationProperties(prefix = "massage")
class MassageProperties(
    val openTime: LocalTime,
    val maxCount: Int,
    val lockKey: String,
)
