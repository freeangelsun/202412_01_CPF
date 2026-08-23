package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.exception.AdmNotificationVersionConflictException;
import com.cpf.admin.opr.dto.AdmNotificationDeliveryStatusResponse;
import com.cpf.admin.opr.service.AdmNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmNotificationControllerAuthenticationTest {

    @Test
    void rejectsMissingAuthenticatedOperator() {
        AdmNotificationController controller =
                new AdmNotificationController(mock(AdmNotificationService.class));
        HttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> controller.findRules(10, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("operator session");
    }

    @Test
    void forwardsAuthenticatedOperatorWithoutCallerOwnedIdentity() {
        AdmNotificationService service = mock(AdmNotificationService.class);
        AdmNotificationController controller = new AdmNotificationController(service);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("adm.operatorId", "operator-a");
        request.setRemoteAddr("127.0.0.1");

        controller.disableRule(1L, "점검", request);

        verify(service).disableRule(1L, "점검", "operator-a", "127.0.0.1");
    }

    @Test
    void forwardsAuthenticatedOperatorAndExpectedVersionForRetry() {
        AdmNotificationService service = mock(AdmNotificationService.class);
        AdmNotificationController controller = new AdmNotificationController(service);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("adm.operatorId", "operator-a");
        request.setRemoteAddr("127.0.0.1");
        when(service.retryDelivery(10L, 7L, "provider 확인", "operator-a", "127.0.0.1"))
                .thenReturn(new AdmNotificationDeliveryStatusResponse(
                        10L,
                        "retry-operation",
                        "request-hash",
                        "PENDING",
                        1,
                        3,
                        null,
                        null,
                        null,
                        8L,
                        null,
                        "operator-a",
                        null));

        AdmNotificationDeliveryStatusResponse body = controller.retryDelivery(
                10L, 7L, "provider 확인", request).getBody();

        assertThat(body).isNotNull();
        assertThat(body.version()).isEqualTo(8L);
        verify(service).retryDelivery(10L, 7L, "provider 확인", "operator-a", "127.0.0.1");
    }

    @Test
    void versionConflictIsMappedToHttp409() {
        ResponseStatus responseStatus =
                AdmNotificationVersionConflictException.class.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.CONFLICT);
    }
}
