package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmIntegrationClosureControllerTest {
    private final AdmIntegrationClosureService service = mock(AdmIntegrationClosureService.class);
    private final AdmIntegrationClosureController controller = new AdmIntegrationClosureController(service);

    @Test
    void optimisticVersionConflictIsHttp409() {
        when(service.executeCorrection(7L, "checker", "execute"))
                .thenReturn(Map.of("execution", Map.of("ownerResultCode", "DQ-VERSION-CONFLICT")));
        var response = controller.executeCorrection(7L, "checker", new AdmIntegrationClosureController.CorrectionExecutionRequest("execute"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void successfulExecutionRemainsHttp200() {
        when(service.executeCorrection(8L, "checker", "execute"))
                .thenReturn(Map.of("execution", Map.of("ownerResultCode", "DQ-CORRECTED")));
        var response = controller.executeCorrection(8L, "checker", new AdmIntegrationClosureController.CorrectionExecutionRequest("execute"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
