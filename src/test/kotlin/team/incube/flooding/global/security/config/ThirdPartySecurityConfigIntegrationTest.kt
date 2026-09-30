package team.incube.flooding.global.security.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import team.incube.flooding.support.IntegrationTestBase

@AutoConfigureMockMvc
class ThirdPartySecurityConfigIntegrationTest : IntegrationTestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `토큰 없이 서드파티 API를 호출하면 401을 반환한다`() {
        mockMvc.get(ATTENDANCES_URL).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `Flooding 스코프가 없는 토큰으로 호출하면 403을 반환한다`() {
        mockMvc
            .get(ATTENDANCES_URL) {
                with(jwt().authorities(SimpleGrantedAuthority("SCOPE_other-app:other")))
            }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `Flooding 스코프가 있는 토큰으로 호출하면 200을 반환한다`() {
        mockMvc
            .get(ATTENDANCES_URL) {
                with(jwt().authorities(SimpleGrantedAuthority("SCOPE_${ThirdPartySecurityConfig.FLOODING_SCOPE}")))
            }.andExpect { status { isOk() } }
    }

    companion object {
        private const val ATTENDANCES_URL = "/third-party/study/attendances"
    }
}
