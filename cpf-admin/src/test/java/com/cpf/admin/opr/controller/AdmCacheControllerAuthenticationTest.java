package com.cpf.admin.opr.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.opr.service.AdmCacheOperationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class AdmCacheControllerAuthenticationTest {
    private final AdmCacheOperationService service = mock(AdmCacheOperationService.class);
    private final AdmCacheController controller = new AdmCacheController(service);
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void refreshRejectsUnauthenticatedRequestBeforeReasonValidationOrSideEffect() {
        when(request.getSession(false)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> controller.refresh("ALL", "audited reason", request));

        verify(service, never()).refresh(any(), any(), any());
    }

    @Test
    void evictKeyRejectsUnauthenticatedRequestBeforeCoordinatorSideEffect() {
        when(request.getSession(false)).thenReturn(null);
        var body = new AdmCacheController.EvictKeyRequest("TENANT", "namespace", "key", 3, "audited reason");

        assertThrows(RuntimeException.class, () -> controller.evictKey(body, request));

        verify(service, never()).evictKey(any(), any(), any(), anyLong(), any(), any());
    }
}
