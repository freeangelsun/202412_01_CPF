package com.cpf.admin.opr.filter;

import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.dto.AdmSession;
import com.cpf.admin.opr.service.AdmSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void 구체다운로드거부권한이넓은조회허용보다우선한다() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(
                Map.of("API_PATH", "/adm/api/remote-logs/**", "HTTP_METHOD", "GET", "ALLOW_YN", "Y"),
                Map.of("API_PATH", "/adm/api/remote-logs/bundle-jobs/*/download",
                        "HTTP_METHOD", "GET", "ALLOW_YN", "N")));
        AdmSession session = new AdmSession(
                TOKEN, "viewer", List.of("ADM_VIEWER"), false,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        AdmApiAuthFilter filter = filter(session, jdbcTemplate);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request("GET", "/adm/api/remote-logs/bundle-jobs/job-01/download"),
                response,
                new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    private AdmApiAuthFilter filter(AdmSession session) {
        return filter(session, mock(JdbcTemplate.class));
    }

    private AdmApiAuthFilter filter(AdmSession session, JdbcTemplate jdbcTemplate) {
        AdmSecurityProperties properties = new AdmSecurityProperties();
        AdmSessionService sessionService = mock(AdmSessionService.class);
        when(sessionService.findValidSession(TOKEN)).thenReturn(Optional.of(session));
        return new AdmApiAuthFilter(properties, sessionService, jdbcTemplate);
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
