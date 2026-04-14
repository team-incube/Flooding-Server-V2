package team.incube.flooding.domain.ai.song.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.ai.song.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.song.presentation.data.request.SongRequest
import team.incube.flooding.domain.ai.song.service.impl.RecommendAiSongServiceImpl
import team.incube.flooding.global.config.AiServerProperties

class RecommendAiSongServiceImplTest {
    private val aiServerUrl = "http://ai-server/song"
    private val properties = AiServerProperties(url = aiServerUrl)

    private fun setup(): Pair<RecommendAiSongServiceImpl, MockRestServiceServer> {
        val builder = RestClient.builder()
        val mockServer = MockRestServiceServer.bindTo(builder).build()
        return RecommendAiSongServiceImpl(builder, properties) to mockServer
    }

    private fun sampleRequest() =
        RecommendAiSongRequest(
            recentSongs =
                listOf(
                    SongRequest("Life", "Ignito"),
                    SongRequest("Newspaper", "이현준"),
                    SongRequest("아침에", "양홍원"),
                    SongRequest("Hip Hop", "dwen"),
                    SongRequest("우리가 바로", "TEAM NY"),
                ),
        )

    @Test
    fun `AI 서버로부터 유튜브 링크 3개를 정상적으로 반환한다`() {
        val (service, mockServer) = setup()

        mockServer
            .expect(requestTo(aiServerUrl))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    """{"youtube_links":["https://www.youtube.com/watch?v=abc","https://www.youtube.com/watch?v=def","https://www.youtube.com/watch?v=ghi"]}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val response = service.execute(sampleRequest())

        assertEquals(3, response.youtubeLinks.size)
        assertEquals("https://www.youtube.com/watch?v=abc", response.youtubeLinks[0])
        assertEquals("https://www.youtube.com/watch?v=def", response.youtubeLinks[1])
        assertEquals("https://www.youtube.com/watch?v=ghi", response.youtubeLinks[2])
        mockServer.verify()
    }

    @Test
    fun `AI 서버가 빈 링크를 반환하면 빈 리스트로 응답한다`() {
        val (service, mockServer) = setup()

        mockServer
            .expect(requestTo(aiServerUrl))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    """{"youtube_links":[]}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val response = service.execute(sampleRequest())

        assertEquals(0, response.youtubeLinks.size)
        mockServer.verify()
    }
}
