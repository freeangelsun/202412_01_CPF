package com.cpf.admin.approval.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("removal")
class AdmApprovalControllerOwnershipTest {

    @Test
    void canonicalControllerIsTheOnlySpringRestOwnerForApprovalApi() {
        Class<?> canonical = com.cpf.admin.approval.controller.AdmApprovalController.class;
        Class<?> legacy = com.cpf.admin.opr.controller.AdmApprovalController.class;
        Class<?> legacyService = com.cpf.admin.opr.service.AdmApprovalEngineService.class;

        assertThat(canonical.isAnnotationPresent(RestController.class)).isTrue();
        assertThat(legacy.isAnnotationPresent(RestController.class)).isFalse();
        assertThat(legacy.isAnnotationPresent(RequestMapping.class)).isFalse();
        assertThat(legacyService.isAnnotationPresent(Service.class)).isFalse();
        assertThat(legacyService.isAnnotationPresent(Deprecated.class)).isTrue();

        RequestMapping mapping = canonical.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).containsExactly("/adm/api/approvals");
    }
}
