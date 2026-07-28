package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdmNotificationControllerAuthenticationTest {
    private final AdmNotificationController controller =
            new AdmNotificationController(mock(AdmNotificationService.class));

    @Test
    void rejectsMissingAuthenticatedOperator() {
        HttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> controller.findRules(10, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("operator session");
    }

    @Test
    void rejectsClaimedOperatorMismatch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("adm.operatorId", "operator-a");

        assertThatThrownBy(() -> controller.disableRule(1L, "점검", "operator-b", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("일치하지 않습니다");
    }
}
