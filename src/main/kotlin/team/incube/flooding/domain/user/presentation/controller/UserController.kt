package team.incube.flooding.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.user.presentation.data.request.PatchUserRoleRequest
import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse
import team.incube.flooding.domain.user.presentation.data.response.SearchUsersResponse
import team.incube.flooding.domain.user.presentation.data.response.UploadUserProfileImageResponse
import team.incube.flooding.domain.user.service.GetMeService
import team.incube.flooding.domain.user.service.PatchUserRoleService
import team.incube.flooding.domain.user.service.SearchUsersService
import team.incube.flooding.domain.user.service.UploadUserProfileImageService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "유저", description = "유저 관련 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val getMeService: GetMeService,
    private val searchUsersService: SearchUsersService,
    private val patchUserRoleService: PatchUserRoleService,
    private val uploadUserProfileImageService: UploadUserProfileImageService,
) {
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 유저의 정보를 반환합니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping("/me")
    fun getMe(): CommonApiResponse<GetMeResponse> = CommonApiResponse.success("OK", getMeService.execute())

    @Operation(
        summary = "학생 검색",
        description =
            "이름(부분 일치) 또는 학번(전방 일치)으로 학생을 검색합니다. " +
                "파라미터를 모두 생략하면 전체 학생을 페이지네이션하여 반환합니다. " +
                "ADMIN 역할은 검색 결과에서 제외됩니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping
    fun searchUsers(
        @Parameter(description = "이름 (부분 일치)")
        @RequestParam(required = false) name: String?,
        @Parameter(description = "학번 (전방 일치, 예: '1' → 1학년 전체, '11' → 1학년 1반)")
        @RequestParam(required = false) studentNumber: String?,
        @Parameter(description = "페이지 정보 (page, size, sort)")
        @PageableDefault(size = 20, sort = ["studentNumber"], direction = Sort.Direction.ASC)
        pageable: Pageable,
    ): CommonApiResponse<Page<SearchUsersResponse>> =
        CommonApiResponse.success("OK", searchUsersService.execute(name, studentNumber, pageable))

    @Operation(
        summary = "유저 프로필 이미지 업로드",
        description = "multipart/form-data로 프로필 이미지를 업로드하고, 사용할 profileImageUrl을 반환합니다.",
    )
    @ApiResponse(responseCode = "201", description = "업로드 성공")
    @ApiResponse(responseCode = "400", description = "지원하지 않는 이미지 파일")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "413", description = "업로드 가능한 파일 크기 초과")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/me/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadUserProfileImage(
        @RequestParam("image") image: MultipartFile,
    ): CommonApiResponse<UploadUserProfileImageResponse> =
        CommonApiResponse.created("OK", uploadUserProfileImageService.execute(image))

    @Operation(
        summary = "유저 권한 변경",
        description = "특정 유저의 권한(Role)을 변경합니다. ADMIN 또는 DORMITORY_MANAGER 역할만 접근 가능합니다.",
    )
    @ApiResponse(responseCode = "204", description = "권한 변경 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 또는 DORMITORY_MANAGER만 접근 가능)")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    @PatchMapping("/{userId}/role")
    fun patchUserRole(
        @Parameter(description = "권한을 변경할 유저 ID") @PathVariable userId: Long,
        @Valid @RequestBody request: PatchUserRoleRequest,
    ): ResponseEntity<Void> {
        patchUserRoleService.execute(userId, request.role)
        return ResponseEntity.noContent().build()
    }
}
