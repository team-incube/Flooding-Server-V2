package team.incube.flooding.domain.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.auth.adapter.RefreshTokenRedisAdapter
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.CreateUserService
import team.incube.flooding.global.auth.provider.JwtProvider
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient
import team.themoment.datagsm.sdk.oauth.model.Student
import team.themoment.datagsm.sdk.oauth.model.TokenResponse
import team.themoment.datagsm.sdk.oauth.model.UserInfo
import java.util.Optional

class SignInServiceTest :
    BehaviorSpec({
        val dataGsmClient = mockk<DataGsmOAuthClient>()
        val userRepository = mockk<UserRepository>()
        val createUserService = mockk<CreateUserService>()
        val jwtProvider = mockk<JwtProvider>()
        val refreshTokenRedisAdapter = mockk<RefreshTokenRedisAdapter>()

        val service =
            SignInService(
                dataGsmClient = dataGsmClient,
                userRepository = userRepository,
                createUserService = createUserService,
                jwtProvider = jwtProvider,
                refreshTokenRedisAdapter = refreshTokenRedisAdapter,
            )

        given("이미 가입된 학생이 DG 로그인할 때") {
            val studentId = 1L
            val student =
                Student().apply {
                    id = studentId
                }
            val oauthUser =
                UserInfo().apply {
                    setIsStudent(true)
                    setStudent(student)
                }
            val user =
                UserJpaEntity(
                    id = studentId,
                    name = "기존 유저",
                    sex = Sex.MAN,
                    email = "student@test.com",
                    studentNumber = 1101,
                    role = Role.ADMIN,
                    dormitoryRoom = 101,
                )

            every { dataGsmClient.exchangeCodeForToken("code", "redirect") } returns
                TokenResponse("dg-access-token", "Bearer", 3600, null, null)
            every { dataGsmClient.getUserInfo("dg-access-token") } returns oauthUser
            every { userRepository.findById(studentId) } returns Optional.of(user)
            every { jwtProvider.generateAccessToken(studentId) } returns "access-token"
            every { jwtProvider.generateRefreshToken(studentId) } returns "refresh-token"
            every { refreshTokenRedisAdapter.save(studentId, "refresh-token") } returns Unit

            `when`("로그인을 처리하면") {
                val response = service.execute("code", "redirect")

                then("유저 생성 로직을 실행하지 않고 기존 권한을 유지한다") {
                    response.accessToken shouldBe "access-token"
                    response.refreshToken shouldBe "refresh-token"
                    user.role shouldBe Role.ADMIN
                    verify(exactly = 0) { createUserService.execute(any()) }
                }
            }
        }
    })
