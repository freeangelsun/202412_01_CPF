package com.cpf.starter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CpfBffCredentialResponseAdviceTest {
    private final CpfBffCredentialResponseAdvice advice = new CpfBffCredentialResponseAdvice();

    @Test
    void stripsCredentialsWithoutExposingSessionId() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("operatorId", "ADM001");
        body.put("accessToken", "access-secret");
        body.put("refreshToken", "refresh-secret");

        Object result = advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse()));

        assertThat(result).isEqualTo(Map.of("operatorId", "ADM001"));
        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession(false).getAttribute(CpfBffSessionBridgeFilter.ACCESS_TOKEN))
                .isEqualTo("access-secret");
        assertThat(request.getSession(false).getAttribute(CpfBffSessionBridgeFilter.REFRESH_TOKEN))
                .isEqualTo("refresh-secret");
        assertThat((Map<?, ?>) result).doesNotContainKeys("sessionId", "accessToken", "refreshToken");
    }

    @Test
    void rejectsNestedOrUnknownCredentialShape() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        Map<String, Object> body = Map.of("data", Map.of("accessToken", "secret"));

        assertThatThrownBy(() -> advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("approved top-level contract");
    }

    @Test
    void leavesAuthenticationErrorBodyWithoutCredentialsUntouched() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/login");
        Map<String, Object> body = Map.of("code", "AUTH_FAILED", "message", "인증에 실패했습니다.");

        Object result = advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse()));

        assertThat(result).isSameAs(body);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void rejectsCredentialArrayToPreventFailOpenLeak() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/adm/api/auth/refresh");
        List<Map<String, String>> body = List.of(Map.of("accessToken", "secret"));

        assertThatThrownBy(() -> advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse())))
                .isInstanceOf(IllegalStateException.class);
    }
}
