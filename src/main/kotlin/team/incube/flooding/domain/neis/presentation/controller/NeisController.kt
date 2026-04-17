package team.incube.flooding.domain.neis.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.neis.presentation.data.request.GetNeisMealsRequest
import team.incube.flooding.domain.neis.presentation.data.request.GetNeisTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisMealsResponse
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisTimetablesResponse
import team.incube.flooding.domain.neis.service.impl.GetNeisMealsServiceImpl
import team.incube.flooding.domain.neis.service.impl.GetNeisTimetablesServiceImpl
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "NEIS", description = "급식/시간표 조회 API")
@Validated
@RestController
@RequestMapping("/v2/neis")
class NeisController(
    private val getNeisMealsService: GetNeisMealsServiceImpl,
    private val getNeisTimetablesService: GetNeisTimetablesServiceImpl,
) {
    @Operation(summary = "급식 조회", description = "지정한 학교의 날짜별 급식 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "급식 조회 성공"),
    )
    @GetMapping("/meals")
    fun getMeals(
        @RequestParam officeCode: String,
        @RequestParam schoolCode: String,
        @RequestParam
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "date는 yyyy-MM-dd 형식이어야 합니다.")
        date: String,
    ): CommonApiResponse<GetNeisMealsResponse> =
        CommonApiResponse.success(
            "OK",
            getNeisMealsService.execute(
                GetNeisMealsRequest(
                    officeCode = officeCode,
                    schoolCode = schoolCode,
                    date = date,
                )
            ),
        )

    @Operation(summary = "시간표 조회", description = "NEIS 원본 API를 사용해 시간표를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "시간표 조회 성공"),
    )
    @GetMapping("/timetables")
    fun getTimetables(
        @Parameter(description = "시도교육청 코드")
        @RequestParam officeCode: String,
        @Parameter(description = "학교 코드")
        @RequestParam schoolCode: String,
        @Parameter(description = "학년")
        @RequestParam
        @Min(value = 1, message = "grade는 1 이상이어야 합니다.")
        @Max(value = 3, message = "grade는 3 이하여야 합니다.")
        grade: Int,
        @Parameter(description = "반")
        @RequestParam
        @Min(value = 1, message = "classNumber는 1 이상이어야 합니다.")
        classNumber: Int,
        @Parameter(description = "조회 날짜(yyyy-MM-dd)")
        @RequestParam
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "date는 yyyy-MM-dd 형식이어야 합니다.")
        date: String,
    ): CommonApiResponse<GetNeisTimetablesResponse> =
        CommonApiResponse.success(
            "OK",
            getNeisTimetablesService.execute(
                GetNeisTimetablesRequest(
                    officeCode = officeCode,
                    schoolCode = schoolCode,
                    grade = grade,
                    classNumber = classNumber,
                    date = date,
                )
            ),
        )
}
