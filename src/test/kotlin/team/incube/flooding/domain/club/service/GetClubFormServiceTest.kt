package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldType
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.service.impl.GetClubFormServiceImpl
import team.themoment.sdk.exception.ExpectedException

class GetClubFormServiceTest :
    BehaviorSpec({
        val clubFormRepository = mockk<ClubFormRepository>()
        val clubFormFieldRepository = mockk<ClubFormFieldRepository>()
        val clubFormFieldOptionRepository = mockk<ClubFormFieldOptionRepository>()

        val service =
            GetClubFormServiceImpl(
                clubFormRepository,
                clubFormFieldRepository,
                clubFormFieldOptionRepository,
            )

        val club =
            ClubJpaEntity(
                id = 1L,
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = null,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = null,
                maxMember = null,
            )

        val form = ClubFormJpaEntity(id = 10L, club = club, title = "신청 폼", description = "설명")

        fun field(
            id: Long,
            order: Int,
            type: ClubFormFieldType = ClubFormFieldType.TEXT,
            required: Boolean = false,
        ) = ClubFormFieldJpaEntity(
            id = id,
            form = form,
            label = "필드$id",
            description = null,
            fieldType = type,
            fieldOrder = order,
            isRequired = required,
        )

        fun option(
            id: Long,
            field: ClubFormFieldJpaEntity,
            optionOrder: Int,
        ) = ClubFormFieldOptionJpaEntity(
            id = id,
            field = field,
            label = "옵션$id",
            value = "value$id",
            optionOrder = optionOrder,
        )

        given("활성 폼이 없을 때") {
            `when`("폼을 조회하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns null

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(1L)
                        }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("폼에 3개의 필드가 있을 때") {
            `when`("폼을 조회하면") {
                then("필드가 order 순으로 반환된다") {
                    val fields = listOf(field(1L, order = 1), field(2L, order = 2), field(3L, order = 3))
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns fields
                    every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(any()) } returns
                        emptyList()

                    val response = service.execute(1L)
                    response.fields shouldHaveSize 3
                    response.fields[0].order shouldBe 1
                    response.fields[1].order shouldBe 2
                    response.fields[2].order shouldBe 3
                }
            }
        }

        given("라디오 필드에 옵션 3개가 있을 때") {
            `when`("폼을 조회하면") {
                then("옵션이 필드에 올바르게 조립된다") {
                    val radioField = field(1L, order = 1, type = ClubFormFieldType.RADIO, required = true)
                    val options =
                        listOf(
                            option(1L, radioField, optionOrder = 0),
                            option(2L, radioField, optionOrder = 1),
                            option(3L, radioField, optionOrder = 2),
                        )
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(radioField)
                    every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L)) } returns
                        options

                    val response = service.execute(1L)
                    response.fields[0].options shouldHaveSize 3
                    response.fields[0].options[0].value shouldBe "value1"
                    response.fields[0].options[1].value shouldBe "value2"
                    response.fields[0].options[2].value shouldBe "value3"
                }
            }
        }

        given("텍스트 필드가 있을 때") {
            `when`("폼을 조회하면") {
                then("옵션은 빈 리스트다") {
                    val textField = field(1L, order = 1, type = ClubFormFieldType.TEXTAREA)
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(textField)
                    every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L)) } returns
                        emptyList()

                    val response = service.execute(1L)
                    response.fields[0].options.shouldBeEmpty()
                }
            }
        }

        given("두 필드에 각각 옵션이 2개씩 있을 때") {
            `when`("폼을 조회하면") {
                then("각 필드에 옵션이 올바르게 매핑된다") {
                    val field1 = field(1L, order = 1, type = ClubFormFieldType.RADIO)
                    val field2 = field(2L, order = 2, type = ClubFormFieldType.DROPDOWN)
                    val options =
                        listOf(
                            option(1L, field1, optionOrder = 0),
                            option(2L, field1, optionOrder = 1),
                            option(3L, field2, optionOrder = 0),
                            option(4L, field2, optionOrder = 1),
                        )
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns
                        listOf(field1, field2)
                    every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L, 2L)) } returns
                        options

                    val response = service.execute(1L)
                    response.fields[0].options shouldHaveSize 2
                    response.fields[1].options shouldHaveSize 2
                    response.fields[0].options[0].value shouldBe "value1"
                    response.fields[1].options[0].value shouldBe "value3"
                }
            }
        }

        given("폼 기본 정보가 있을 때") {
            `when`("폼을 조회하면") {
                then("폼 기본 정보가 올바르게 반환된다") {
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns emptyList()
                    every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(emptyList()) } returns
                        emptyList()

                    val response = service.execute(1L)
                    response.formId shouldBe 10L
                    response.title shouldBe "신청 폼"
                    response.description shouldBe "설명"
                }
            }
        }
    })
