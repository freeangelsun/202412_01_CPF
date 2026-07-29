package com.cpf.admin.opr.filter;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.dto.AdmSession;
import com.cpf.admin.opr.service.AdmSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 강제 비밀번호 변경 세션이 허용 API 외의 ADM 기능에 접근하지 못하는지 검증합니다.
 */
class AdmApiAuthFilterTest {
    private static final String TOKEN = "test-token";

    @Test
    void readinessAndLivenessArePublicReadOnlyProbes() throws Exception {
        AdmApiAuthFilter filter = filter(forcedSession());
        for (String path : List.of(
                "/adm/api/health",
                "/adm/api/health/liveness",
                "/adm/api/health/readiness")) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, new MockHttpServletResponse(), chain);

            assertThat(chain.getRequest()).as(path).isNotNull();
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(
                new MockHttpServletRequest("POST", "/adm/api/health/readiness"),
                response,
                new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(401);
    }

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
    void authenticatedSessionCanUseAuthSelfServiceWithoutMenuPermission() throws Exception {
        AdmApiAuthFilter filter = filter(normalSession("ADM_VIEWER"));

        for (RequestCase requestCase : List.of(
                new RequestCase("GET", "/adm/api/auth/me"),
                new RequestCase("POST", "/adm/api/auth/logout"))) {
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(
                    request(requestCase.method(), requestCase.path()),
                    new MockHttpServletResponse(),
                    chain);

            assertThat(chain.getRequest()).as(requestCase.toString()).isNotNull();
        }
    }

    @Test
    void authSelfServiceStillRequiresAuthenticationAndExactHttpMethod() throws Exception {
        AdmApiAuthFilter filter = filter(normalSession("ADM_VIEWER"));

        for (RequestCase requestCase : List.of(
                new RequestCase("GET", "/adm/api/auth/me"),
                new RequestCase("POST", "/adm/api/auth/logout"))) {
            MockHttpServletResponse unauthenticated = new MockHttpServletResponse();
            filter.doFilter(
                    new MockHttpServletRequest(requestCase.method(), requestCase.path()),
                    unauthenticated,
                    new MockFilterChain());
            assertThat(unauthenticated.getStatus()).as(requestCase.toString()).isEqualTo(401);
        }

        for (RequestCase requestCase : List.of(
                new RequestCase("POST", "/adm/api/auth/me"),
                new RequestCase("GET", "/adm/api/auth/logout"))) {
            MockHttpServletResponse wrongMethod = new MockHttpServletResponse();
            filter.doFilter(
                    request(requestCase.method(), requestCase.path()),
                    wrongMethod,
                    new MockFilterChain());
            assertThat(wrongMethod.getStatus()).as(requestCase.toString()).isEqualTo(403);
        }
    }

    @Test
    void memoryFallbackRecognizesCurrentAdmRouteMenuContract() throws Exception {
        AdmApiAuthFilter filter = filter(normalSession("ADM_ADMIN"));

        for (RequestCase requestCase : List.of(
                new RequestCase("GET", "/adm/api/v1/system/version"),
                new RequestCase("GET", "/adm/api/batch-runtime/instances"),
                new RequestCase("GET", "/adm/api/runtime-control/status"),
                new RequestCase("GET", "/adm/api/maintenance/actions"),
                new RequestCase("GET", "/adm/api/incidents"),
                new RequestCase("GET", "/adm/api/secrets/providers"),
                new RequestCase("GET", "/adm/api/approvals/policies"),
                new RequestCase("GET", "/adm/api/break-glass"))) {
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(
                    request(requestCase.method(), requestCase.path()),
                    new MockHttpServletResponse(),
                    chain);

            assertThat(chain.getRequest()).as(requestCase.toString()).isNotNull();
        }
    }

    @Test
    void batchRuntimeDoesNotInheritBroaderBatchPrefixPermission() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    Object[] arguments = invocation.getArguments();
                    return "BATCH_RUNTIME".equals(arguments[arguments.length - 1]) ? 1 : 0;
                });
        AdmApiAuthFilter filter = filter(
                normalSession("ADM_VIEWER"),
                jdbcTemplate,
                persistencePolicy("DATABASE"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                request("GET", "/adm/api/batch-runtime/instances"),
                new MockHttpServletResponse(),
                chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void canonicalDbApiPermissionCanRegisterExtensionWithoutStaticMapDuplication() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(
                Map.of("API_PATH", "/adm/api/extensions/**", "HTTP_METHOD", "GET", "ALLOW_YN", "Y")));
        AdmApiAuthFilter filter = filter(
                normalSession("ADM_VIEWER"),
                jdbcTemplate,
                persistencePolicy("DATABASE"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                request("GET", "/adm/api/extensions/tasks"),
                new MockHttpServletResponse(),
                chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void unregisteredAdmApiRemainsDeniedForAdmin() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        AdmApiAuthFilter filter = filter(
                normalSession("ADM_ADMIN"),
                jdbcTemplate,
                persistencePolicy("DATABASE"));

        for (String path : List.of(
                "/adm/api/not-registered",
                "/adm/api/auth/not-registered",
                "/adm/api/batch-rogue")) {
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request("GET", path), response, new MockFilterChain());

            assertThat(response.getStatus()).as(path).isEqualTo(403);
        }
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
        return filter(session, unavailableJdbcTemplate(), persistencePolicy("MEMORY"));
    }

    private AdmApiAuthFilter filter(AdmSession session, JdbcTemplate jdbcTemplate) {
        return filter(session, jdbcTemplate, persistencePolicy("MEMORY"));
    }

    private AdmApiAuthFilter filter(
            AdmSession session,
            JdbcTemplate jdbcTemplate,
            AdmPersistencePolicy persistencePolicy) {
        AdmSecurityProperties properties = new AdmSecurityProperties();
        AdmSessionService sessionService = mock(AdmSessionService.class);
        when(sessionService.findValidSession(TOKEN)).thenReturn(Optional.of(session));
        return new AdmApiAuthFilter(properties, sessionService, jdbcTemplate, persistencePolicy);
    }

    private JdbcTemplate unavailableJdbcTemplate() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DataAccessResourceFailureException unavailable =
                new DataAccessResourceFailureException("test permission database unavailable");
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenThrow(unavailable);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenThrow(unavailable);
        return jdbcTemplate;
    }

    private AdmPersistencePolicy persistencePolicy(String mode) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        environment.setProperty("cpf.adm.persistence.mode", mode);
        return new AdmPersistencePolicy(environment);
    }

    private AdmSession forcedSession() {
        LocalDateTime now = LocalDateTime.now();
        return new AdmSession(TOKEN, "admin", List.of("ADM_ADMIN"), true, now, now.plusHours(1));
    }

    private AdmSession normalSession(String roleId) {
        LocalDateTime now = LocalDateTime.now();
        return new AdmSession(TOKEN, "operator", List.of(roleId), false, now, now.plusHours(1));
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }

    private record RequestCase(String method, String path) {
    }
}
