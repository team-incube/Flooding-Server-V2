package team.incube.flooding.global.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "youtube",
    url = "https://www.googleapis.com/youtube/v3",
    configuration = [YoutubeApiClientConfig::class],
)
interface YoutubeApiClient {
    @GetMapping("/videos")
    fun getVideos(
        @RequestParam("part") part: String,
        @RequestParam("id") id: String,
    ): YoutubeVideoListResponse
}

data class YoutubeVideoListResponse(
    val items: List<YoutubeVideoItem>?,
)

data class YoutubeVideoItem(
    val snippet: YoutubeSnippet?,
    val contentDetails: YoutubeContentDetails?,
)

data class YoutubeSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: YoutubeThumbnails?,
)

data class YoutubeContentDetails(
    val duration: String?,
)

data class YoutubeThumbnails(
    val default: YoutubeThumbnail?,
    val high: YoutubeThumbnail?,
    val maxres: YoutubeThumbnail?,
)

data class YoutubeThumbnail(
    val url: String?,
)
