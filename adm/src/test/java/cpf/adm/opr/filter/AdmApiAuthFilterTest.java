package cpf.adm.opr.filter;

import cpf.adm.config.AdmSecurityProperties;
import cpf.adm.opr.dto.AdmSession;
import cpf.adm.opr.service.AdmSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 강제 비밀번호 변경 세션이 허용 API 외의 ADM 기능에 접근하지 못하는지 검증합니다.
 */
class AdmApiAuthFilterTest {
    private static final String TOKEN = "test-token";

    @Test
    void forcedSessionCannotReadOperationalApi() throws Exception {
        AdmApiAuthFilter filter = filter(forcedSession());
        MockHttpServletRequest request = request("GET", "/adm/api/logs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("비밀번호를 먼저 변경");
    }

    @Test
    void forcedSessionCanChangeOnlyItsOwnPassword() throws Exception {
        AdmApiAuthFilter filter = filter(forcedSession());
        MockHttpServletRequest ownRequest = request("POST", "/adm/api/operators/admin/password");
        MockHttpServletResponse ownResponse = new MockHttpServletResponse();
        MockFilterChain ownChain = new MockFilterChain();

        filter.doFilter(ownRequest, ownResponse, ownChain);

        assertThat(ownChain.getRequest()).isNotNull();
        assertThat(ownRequest.getAttribute("adm.operatorId")).isEqualTo("admin");

        MockHttpServletResponse otherResponse = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/adm/api/operators/other/password"), otherResponse, new MockFilterChain());
        assertThat(otherResponse.getStatus()).isEqualTo(403);
    }

    @Test
    void forcedSessionCanReadItsSessionMetadata() throws Exception {
        AdmApiAuthFilter filter = filter(forcedSession());
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("GET", "/adm/api/auth/me"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    private AdmApiAuthFilter filter(AdmSession session) {
        AdmSecurityProperties properties = new AdmSecurityProperties();
        AdmSessionService sessionService = mock(AdmSessionService.class);
        when(sessionService.findValidSession(TOKEN)).thenReturn(Optional.of(session));
        return new AdmApiAuthFilter(properties, sessionService, mock(JdbcTemplate.class));
    }

    private AdmSession forcedSession() {
        LocalDateTime now = LocalDateTime.now();
        return new AdmSession(TOKEN, "admin", List.of("ADM_ADMIN"), true, now, now.plusHours(1));
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }
}
