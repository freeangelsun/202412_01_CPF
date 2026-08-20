package com.cpf.backoffice.online.auth.filter;

import com.cpf.backoffice.online.auth.dto.BackofficeAuthorizationResult;
import com.cpf.backoffice.online.auth.dto.BackofficeOperatorResponse;
import com.cpf.backoffice.online.auth.service.BackofficeAuthService;
import com.cpf.backoffice.online.auth.permission.BackofficePermissionManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackofficeApiAuthFilterTest {

    private final BackofficeAuthService authService = mock(BackofficeAuthService.class);
    private final BackofficeApiAuthFilter filter =
            new BackofficeApiAuthFilter(authService, new BackofficePermissionManifest(new ObjectMapper()));

    @Test
    void attachmentDownloadRequiresServerDownloadPermission() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/v1/backoffice/attachments/10/download");
        when(authService.authorize("Bearer token", "ATTACHMENT", "DOWNLOAD"))
                .thenReturn(authorization("operator01", "ATTACHMENT", "DOWNLOAD"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(authService).authorize("Bearer token", "ATTACHMENT", "DOWNLOAD");
        assertThat(request.getAttribute("backoffice.operatorId")).isEqualTo("operator01");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void notificationCreateRequiresServerWritePermission() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/v1/backoffice/notifications");
        when(authService.authorize("Bearer token", "SETTING", "WRITE"))
                .thenReturn(authorization("operator02", "SETTING", "WRITE"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(authService).authorize("Bearer token", "SETTING", "WRITE");
    }

    @Test
    void approvalParticipantDecisionRequiresDedicatedDecisionPermission() throws Exception {
        MockHttpServletRequest request =
                request("POST", "/api/v1/backoffice/approvals/submissions/7/decisions");
        when(authService.authorize("Bearer token", "APPROVAL", "DECIDE"))
                .thenReturn(authorization("approver01", "APPROVAL", "DECIDE"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(authService).authorize("Bearer token", "APPROVAL", "DECIDE");
    }


    @Test
    void approvalSimulationRequiresDedicatedSimulatePermission() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/v1/backoffice/approvals/simulate");
        when(authService.authorize("Bearer token", "APPROVAL", "SIMULATE"))
                .thenReturn(authorization("simulator01", "APPROVAL", "SIMULATE"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(authService).authorize("Bearer token", "APPROVAL", "SIMULATE");
        assertThat(request.getAttribute("backoffice.actionCode")).isEqualTo("SIMULATE");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void approvalSimulationDirectCallIsDeniedWithoutSimulatePermission() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/v1/backoffice/approvals/simulate");
        when(authService.authorize("Bearer token", "APPROVAL", "SIMULATE"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "SIMULATE permission required"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(authService).authorize("Bearer token", "APPROVAL", "SIMULATE");
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("권한 확인에 실패");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void productAdminAuthorizationGroupProtectsUserRoleMenuAndPermissionApis() throws Exception {
        for (String uri : new String[] {
                "/api/v1/backoffice/admin-users/page",
                "/api/v1/backoffice/menus/page",
                "/api/v1/backoffice/roles/page",
                "/api/v1/backoffice/permissions/page",
                "/api/v1/backoffice/directory/user-roles/page"
        }) {
            MockHttpServletRequest request = request("GET", uri);
            when(authService.authorize("Bearer token", "AUTHORIZATION", "READ"))
                    .thenReturn(authorization("backoffice-admin", "AUTHORIZATION", "READ"));
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getStatus()).as(uri).isEqualTo(200);
        }
        verify(authService, org.mockito.Mockito.times(5))
                .authorize("Bearer token", "AUTHORIZATION", "READ");
    }

    @Test
    void unregisteredApiResourceFailsClosedBeforeController() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request("GET", "/api/v1/backoffice/not-registered"),
                response,
                new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        return request;
    }

    private BackofficeAuthorizationResult authorization(
            String loginId, String menuCode, String actionCode) {
        BackofficeOperatorResponse operator = new BackofficeOperatorResponse(
                1L,
                loginId,
                "테스트 운영자",
                "MBW_MANAGER",
                "ACTIVE",
                "Y",
                "N",
                0,
                "N",
                null,
                null,
                List.of(menuCode),
                List.of(menuCode + ":" + actionCode));
        return new BackofficeAuthorizationResult(operator, menuCode, actionCode);
    }
}
