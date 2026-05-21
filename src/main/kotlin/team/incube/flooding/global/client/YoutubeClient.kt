package team.incube.flooding.global.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI

@Component
class YoutubeClient(
    private val youtubeApiClient: YoutubeApiClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

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
            youtubeApiClient
                .getVideos(part = "snippet,contentDetails", id = videoId)
                .items
                ?.firstOrNull()
                ?.let { item ->
                    val snippet = item.snippet ?: return@let null
                    val isoDuration = item.contentDetails?.duration ?: "PT0S"
                    VideoInfo(
                        title = snippet.title,
                        artist = snippet.channelTitle,
                        duration = isoDuration,
                        durationText = formatDuration(isoDuration),
                        thumbnailUrl =
                            snippet.thumbnails?.maxres?.url
                                ?: snippet.thumbnails?.high?.url
                                ?: snippet.thumbnails?.default?.url
                                ?: "",
                        videoUrl = "https://www.youtube.com/watch?v=$videoId",
                    )
                }
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
}
