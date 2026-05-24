package team.incube.flooding.global.client

import com.sun.net.httpserver.HttpServer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.net.InetSocketAddress

class YoutubeClientTest :
    BehaviorSpec({
        given("youtu.be 공유 링크에 추가 쿼리 파라미터가 있을 때") {
            val server = HttpServer.create(InetSocketAddress(0), 0)
            val requests = mutableListOf<String>()
            server.createContext("/videos") { exchange ->
                requests.add(exchange.requestURI.rawQuery)
                val response =
                    """
                    {
                      "items": [
                        {
                          "snippet": {
                            "title": "테스트 음악",
                            "channelTitle": "테스트 채널",
                            "thumbnails": {
                              "default": { "url": "https://img.youtube.com/default.jpg" },
                              "high": { "url": "https://img.youtube.com/high.jpg" }
                            }
                          },
                          "contentDetails": { "duration": "PT4M05S" }
                        }
                      ]
                    }
                    """.trimIndent()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { it.write(response.toByteArray()) }
            }
            server.start()
            val youtubeClient = YoutubeClient("test-key", "http://localhost:${server.address.port}")

            `when`("영상 정보를 조회하면") {
                then("영상 ID만 YouTube API에 전달한다") {
                    val result = youtubeClient.getVideoInfo("https://youtu.be/g6U2SS-ZMy8?si=59Kg1Qoy0yw4633U")

                    result?.title shouldBe "테스트 음악"
                    result?.artist shouldBe "테스트 채널"
                    result?.duration shouldBe "PT4M05S"
                    result?.durationText shouldBe "4:05"
                    result?.thumbnailUrl shouldBe "https://img.youtube.com/high.jpg"
                    result?.videoUrl shouldBe "https://www.youtube.com/watch?v=g6U2SS-ZMy8"
                    requests.single() shouldBe "part=snippet,contentDetails&id=g6U2SS-ZMy8&key=test-key"
                }
            }

            afterSpec {
                server.stop(0)
            }
        }
    })
