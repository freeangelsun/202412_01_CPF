package cpf.bza.auth.filter;

import cpf.bza.auth.service.BzaAuthService;
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
    private final BzaApiAuthFilter filter = new BzaApiAuthFilter(authService);

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
        when(authService.authorize("Bearer token", "NOTIFICATION", "WRITE"))
                .thenReturn(Map.of("loginId", "operator02"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(authService).authorize("Bearer token", "NOTIFICATION", "WRITE");
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        return request;
    }
}
