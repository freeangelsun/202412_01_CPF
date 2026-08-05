package com.cpf.admin.approval.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V9 approval REST/OpenAPI surface must have exactly one Spring owner.
 */
class AdmApprovalRouteOwnershipTest {

    @Test
    void canonicalControllerIsTheOnlySpringRestOwner() {
        Class<?> canonical = com.cpf.admin.approval.controller.AdmApprovalController.class;
        Class<?> legacy = com.cpf.admin.opr.controller.AdmApprovalController.class;

        assertThat(canonical.isAnnotationPresent(RestController.class)).isTrue();
        assertThat(canonical.getAnnotation(RequestMapping.class).value())
                .containsExactly("/adm/api/approvals");

        assertThat(legacy.isAnnotationPresent(RestController.class)).isFalse();
        assertThat(legacy.isAnnotationPresent(RequestMapping.class)).isFalse();
    }
}
