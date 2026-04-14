package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubApprovalStatus
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.PatchClubApprovalServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class PatchClubApprovalServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val service = PatchClubApprovalServiceImpl(clubRepository)

        fun club(approvalStatus: ClubApprovalStatus) =
            ClubJpaEntity(
                id = 1L,
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = null,
                imageUrl = null,
                status = ClubStatus.NEW,
                description = null,
                maxMember = null,
                approvalStatus = approvalStatus,
            )

        given("존재하지 않는 clubId로 요청할 때") {
            `when`("승인/거부하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { clubRepository.findById(99L) } returns Optional.empty()

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(99L, PatchClubApprovalRequest(approved = true, reason = "승인"))
                        }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("approvalStatus가 PENDING이 아닌 동아리에") {
            `when`("승인/거부하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    every { clubRepository.findById(1L) } returns Optional.of(club(ClubApprovalStatus.APPROVED))

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(1L, PatchClubApprovalRequest(approved = true, reason = "승인"))
                        }
                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("PENDING 상태의 동아리에") {
            `when`("approved=true로 요청하면") {
                then("APPROVED 상태로 변경된다") {
                    every { clubRepository.findById(1L) } returns Optional.of(club(ClubApprovalStatus.PENDING))
                    every { clubRepository.updateApprovalStatus(1L, ClubApprovalStatus.APPROVED) } returns Unit

                    val response = service.execute(1L, PatchClubApprovalRequest(approved = true, reason = "승인"))

                    response.clubId shouldBe 1L
                    response.status shouldBe ClubApprovalStatus.APPROVED
                    verify(exactly = 1) { clubRepository.updateApprovalStatus(1L, ClubApprovalStatus.APPROVED) }
                }
            }

            `when`("approved=false로 요청하면") {
                then("REJECTED 상태로 변경된다") {
                    every { clubRepository.findById(1L) } returns Optional.of(club(ClubApprovalStatus.PENDING))
                    every { clubRepository.updateApprovalStatus(1L, ClubApprovalStatus.REJECTED) } returns Unit

                    val response = service.execute(1L, PatchClubApprovalRequest(approved = false, reason = "부적합"))

                    response.clubId shouldBe 1L
                    response.status shouldBe ClubApprovalStatus.REJECTED
                    verify(exactly = 1) { clubRepository.updateApprovalStatus(1L, ClubApprovalStatus.REJECTED) }
                }
            }
        }
    })
