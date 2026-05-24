package team.incube.flooding.global.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URI

@Component
class YoutubeClient internal constructor(
    @Value("\${youtube.api-key}") private val apiKey: String,
    baseUrl: String = "https://www.googleapis.com/youtube/v3",
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .build()

    data class VideoInfo(
        val title: String,
        val artist: String,
        val duration: String,
        val durationText: String,
        val thumbnailUrl: String,
        val videoUrl: String,
    )

    fun getVideoInfo(videoUrl: String): VideoInfo? {
        val videoId = extractVideoId(videoUrl) ?: return null
        return runCatching {
            val response =
                restClient
                    .get()
                    .uri {
                        it
                            .path("/videos")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("id", videoId)
                            .queryParam("key", apiKey)
                            .build()
                    }.retrieve()
                    .body(YoutubeVideoListResponse::class.java)
            val item = response?.items?.firstOrNull()
            if (item == null) {
                log.warn("YouTube API 응답에 영상 정보 없음: videoId=$videoId")
                return@runCatching null
            }
            val snippet = item.snippet ?: return@runCatching null
            val title =
                snippet.title ?: run {
                    log.warn("YouTube API snippet에 title 없음: videoId=$videoId")
                    return@runCatching null
                }
            val channelTitle =
                snippet.channelTitle ?: run {
                    log.warn("YouTube API snippet에 channelTitle 없음: videoId=$videoId")
                    return@runCatching null
                }
            val isoDuration = item.contentDetails?.duration ?: "PT0S"
            VideoInfo(
                title = title,
                artist = channelTitle,
                duration = isoDuration,
                durationText = formatDuration(isoDuration),
                thumbnailUrl =
                    snippet.thumbnails?.maxres?.url
                        ?: snippet.thumbnails?.high?.url
                        ?: snippet.thumbnails?.default?.url
                        ?: "",
                videoUrl = "https://www.youtube.com/watch?v=$videoId",
            )
        }.onFailure { log.error("YouTube 영상 정보 조회 실패: videoId=$videoId", it) }
            .getOrNull()
    }

    private fun formatDuration(isoDuration: String): String {
        val hours =
            Regex("(\\d+)H")
                .find(isoDuration)
                ?.groupValues
                ?.get(1)
                ?.toLongOrNull() ?: 0L
        val minutes =
            Regex("(\\d+)M")
                .find(isoDuration)
                ?.groupValues
                ?.get(1)
                ?.toLongOrNull() ?: 0L
        val seconds =
            Regex("(\\d+)S")
                .find(isoDuration)
                ?.groupValues
                ?.get(1)
                ?.toLongOrNull() ?: 0L
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
    }

    private fun extractVideoId(url: String): String? =
        runCatching {
            val uri = URI(url)
            when {
                uri.host?.contains("youtu.be") == true -> {
                    uri.path.trimStart('/')
                }

                uri.host?.contains("youtube.com") == true -> {
                    val path = uri.path ?: ""
                    if (path.startsWith("/embed/") || path.startsWith("/v/")) {
                        path.split("/").lastOrNull()
                    } else {
                        uri.query
                            ?.split("&")
                            ?.firstOrNull { it.startsWith("v=") }
                            ?.removePrefix("v=")
                    }
                }

                else -> {
                    null
                }
            }
        }.getOrNull()

    private data class YoutubeVideoListResponse(
        val items: List<YoutubeVideoItem>?,
    )

    private data class YoutubeVideoItem(
        val snippet: YoutubeSnippet?,
        val contentDetails: YoutubeContentDetails?,
    )

    private data class YoutubeSnippet(
        val title: String?,
        val channelTitle: String?,
        val thumbnails: YoutubeThumbnails?,
    )

    private data class YoutubeContentDetails(
        val duration: String?,
    )

    private data class YoutubeThumbnails(
        val default: YoutubeThumbnail?,
        val high: YoutubeThumbnail?,
        val maxres: YoutubeThumbnail?,
    )

    private data class YoutubeThumbnail(
        val url: String?,
    )
}
