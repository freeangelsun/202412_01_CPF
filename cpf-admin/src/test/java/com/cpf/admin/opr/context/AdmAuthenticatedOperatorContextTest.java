package com.cpf.admin.opr.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmAuthenticatedOperatorContextTest {
    private final AdmAuthenticatedOperatorContext context =
            new AdmAuthenticatedOperatorContext();

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void returnsOnlyOperatorVerifiedByAdmAuthenticationFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("adm.operatorId", " operator-a ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(context.currentOperatorId()).isEqualTo("operator-a");
    }

    @Test
    void failsClosedWithoutVerifiedOperator() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThatThrownBy(context::currentOperatorId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("operator context");
    }
}
