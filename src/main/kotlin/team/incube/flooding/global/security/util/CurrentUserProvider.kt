package team.incube.flooding.global.security.util

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.themoment.sdk.exception.ExpectedException

@Component
class CurrentUserProvider {

    fun getCurrentUser(): UserJpaEntity {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ExpectedException("인증 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

        return authentication.principal as? UserJpaEntity
            ?: throw ExpectedException("인증 정보가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
    }
}
