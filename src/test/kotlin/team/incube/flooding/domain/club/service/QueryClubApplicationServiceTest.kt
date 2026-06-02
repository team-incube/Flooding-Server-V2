package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubApprovalStatus
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.service.impl.QueryClubApplicationServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class QueryClubApplicationServiceTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerTest

        val clubJpaRepository = mockk<ClubJpaRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service = QueryClubApplicationServiceImpl(clubJpaRepository, currentUserProvider)

        fun user(role: Role) =
            UserJpaEntity(
                id = 1L,
                name = "관리자",
                sex = Sex.MAN,
                email = "admin@test.com",
                studentNumber = 10101,
                role = role,
                dormitoryRoom = 101,
            )

        fun club(
            id: Long,
            approvalStatus: ClubApprovalStatus,
        ) = ClubJpaEntity(
            id = id,
            name = "테스트 동아리 $id",
            type = ClubType.MAJOR_CLUB,
            leader = null,
            imageUrl = null,
            status = ClubStatus.NEW,
            description = "설명",
            maxMember = 10,
            approvalStatus = approvalStatus,
        )

        given("ADMIN 사용자가 동아리 개설 신청 목록을 조회할 때") {
            `when`("반려된 신청이 포함되어 있으면") {
                then("응답에 approvalStatus가 포함된다") {
                    val rejectedClub = club(1L, ClubApprovalStatus.REJECTED)
                    every { currentUserProvider.getCurrentUser() } returns user(Role.ADMIN)
                    every { clubJpaRepository.findAllByStatus(ClubStatus.NEW) } returns listOf(rejectedClub)

                    val response = service.execute()

                    response.clubs.size shouldBe 1
                    response.clubs[0].id shouldBe 1L
                    response.clubs[0].approvalStatus shouldBe ClubApprovalStatus.REJECTED
                }
            }
        }

        given("ADMIN이 아닌 사용자가 동아리 개설 신청 목록을 조회할 때") {
            `when`("목록 조회를 요청하면") {
                then("FORBIDDEN 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user(Role.GENERAL_STUDENT)

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute()
                        }

                    exception.statusCode shouldBe HttpStatus.FORBIDDEN
                }
            }
        }
    })
