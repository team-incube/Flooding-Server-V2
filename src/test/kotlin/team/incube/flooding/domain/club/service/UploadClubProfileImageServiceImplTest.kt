package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.UploadClubProfileImageServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.incube.flooding.global.util.FileStorageService
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class UploadClubProfileImageServiceImplTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val fileStorageService = mockk<FileStorageService>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service = UploadClubProfileImageServiceImpl(clubRepository, fileStorageService, currentUserProvider)

        beforeEach { clearAllMocks() }

        fun user(
            id: Long,
            role: Role,
        ) = UserJpaEntity(
            id = id,
            name = "테스트",
            sex = Sex.MAN,
            email = "test@test.com",
            studentNumber = 10101,
            role = role,
            dormitoryRoom = 101,
        )

        fun club(leader: UserJpaEntity?) =
            ClubJpaEntity(
                id = 1L,
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = leader,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = "기존 설명",
                maxMember = 10,
            )

        fun image() = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

        given("존재하지 않는 clubId로 요청할 때") {
            `when`("업로드하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { clubRepository.findById(99L) } returns Optional.empty()

                    val exception = shouldThrow<ExpectedException> { service.execute(99L, image()) }

                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("권한이 없는 일반 학생이") {
            `when`("본인 동아리가 아닌 동아리 이미지를 업로드하면") {
                then("FORBIDDEN 예외가 발생한다") {
                    val leader = user(2L, Role.GENERAL_STUDENT)
                    val currentUser = user(1L, Role.GENERAL_STUDENT)
                    every { clubRepository.findById(1L) } returns Optional.of(club(leader))
                    every { currentUserProvider.getCurrentUser() } returns currentUser

                    val exception = shouldThrow<ExpectedException> { service.execute(1L, image()) }

                    exception.statusCode shouldBe HttpStatus.FORBIDDEN
                }
            }
        }

        given("동아리 리더가") {
            `when`("본인 동아리 이미지를 업로드하면") {
                then("이미지 URL이 변경된다") {
                    val leader = user(1L, Role.GENERAL_STUDENT)
                    val targetClub = club(leader)
                    every { clubRepository.findById(1L) } returns Optional.of(targetClub)
                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { fileStorageService.store(any(), "clubs") } returns "/images/clubs/profile.png"

                    val response = service.execute(1L, image())

                    response.imageUrl shouldBe "/images/clubs/profile.png"
                    targetClub.imageUrl shouldBe "/images/clubs/profile.png"
                }
            }
        }

        given("ADMIN이") {
            `when`("동아리 이미지를 업로드하면") {
                then("이미지 URL이 변경된다") {
                    val admin = user(1L, Role.ADMIN)
                    val targetClub = club(null)
                    every { clubRepository.findById(1L) } returns Optional.of(targetClub)
                    every { currentUserProvider.getCurrentUser() } returns admin
                    every { fileStorageService.store(any(), "clubs") } returns "/images/clubs/admin.png"

                    val response = service.execute(1L, image())

                    response.imageUrl shouldBe "/images/clubs/admin.png"
                    targetClub.imageUrl shouldBe "/images/clubs/admin.png"
                }
            }
        }

        given("STUDENT_COUNCIL이") {
            `when`("동아리 이미지를 업로드하면") {
                then("이미지 URL이 변경된다") {
                    val council = user(1L, Role.STUDENT_COUNCIL)
                    val targetClub = club(null)
                    every { clubRepository.findById(1L) } returns Optional.of(targetClub)
                    every { currentUserProvider.getCurrentUser() } returns council
                    every { fileStorageService.store(any(), "clubs") } returns "/images/clubs/council.png"

                    val response = service.execute(1L, image())

                    response.imageUrl shouldBe "/images/clubs/council.png"
                    targetClub.imageUrl shouldBe "/images/clubs/council.png"
                }
            }
        }
    })
