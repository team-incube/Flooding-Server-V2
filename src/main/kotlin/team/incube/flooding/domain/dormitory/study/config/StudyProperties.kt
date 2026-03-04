package team.incube.flooding.domain.dormitory.study.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalTime

@ConfigurationProperties(prefix = "study")
class StudyProperties(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val maxCount: Int
)