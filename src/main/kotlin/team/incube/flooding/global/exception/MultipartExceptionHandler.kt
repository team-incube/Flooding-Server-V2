package team.incube.flooding.global.exception

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import team.themoment.sdk.response.CommonApiResponse

@RestControllerAdvice
class MultipartExceptionHandler(
    @Value("\${spring.servlet.multipart.max-file-size}")
    private val maxFileSize: String,
) {
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(): ResponseEntity<CommonApiResponse<Nothing>> =
        ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(
                CommonApiResponse.error(
                    "업로드 가능한 파일 크기는 최대 ${maxFileSize}입니다.",
                    HttpStatus.PAYLOAD_TOO_LARGE,
                ),
            )
}
