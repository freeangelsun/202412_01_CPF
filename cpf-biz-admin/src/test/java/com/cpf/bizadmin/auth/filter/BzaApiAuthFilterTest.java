package com.cpf.bizadmin.auth.filter;

import com.cpf.bizadmin.auth.service.BzaAuthService;
import com.cpf.bizadmin.auth.permission.BzaPermissionManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BzaApiAuthFilterTest {

    private final BzaAuthService authService = mock(BzaAuthService.class);
    private final BzaApiAuthFilter filter =
            new BzaApiAuthFilter(authService, new BzaPermissionManifest(new ObjectMapper()));

    @Test
    void attachmentDownloadRequiresServerDownloadPermission() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/bza/attachments/10/download");
        when(authService.authorize("Bearer token", "ATTACHMENT", "DOWNLOAD"))
                .thenReturn(Map.of("loginId", "operator01"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(authService).authorize("Bearer token", "ATTACHMENT", "DOWNLOAD");
        assertThat(request.getAttribute("bza.operatorId")).isEqualTo("operator01");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void notificationCreateRequiresServerWritePermission() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/bza/notifications");
        when(authService.authorize("Bearer token", "SETTING", "WRITE"))
                .thenReturn(Map.of("loginId", "operator02"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(authService).authorize("Bearer token", "SETTING", "WRITE");
    }

    @Test
    void productAdminAuthorizationGroupProtectsUserRoleMenuAndPermissionApis() throws Exception {
        for (String uri : new String[] {
                "/api/bza/admin-users/page",
                "/api/bza/menus/page",
                "/api/bza/roles/page",
                "/api/bza/permissions/page",
                "/api/bza/directory/user-roles/page"
        }) {
            MockHttpServletRequest request = request("GET", uri);
            when(authService.authorize("Bearer token", "AUTHORIZATION", "READ"))
                    .thenReturn(Map.of("loginId", "bza-admin"));
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
                request("GET", "/api/bza/not-registered"),
                response,
                new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        return request;
    }
}
