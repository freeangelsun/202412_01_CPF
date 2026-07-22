package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmCenterCutOperationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmCenterCutControllerTest {

    private final AdmCenterCutOperationService service = mock(AdmCenterCutOperationService.class);
    private final AdmCenterCutController controller = new AdmCenterCutController(service);

    @Test
    void findJobsDelegatesToOperationService() {
        when(service.findJobs()).thenReturn(List.of(Map.of("centerCutJobId", "CPF_REF_CENTER_CUT_SAMPLE_JOB")));

        ResponseEntity<List<Map<String, Object>>> response = controller.findJobs();

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).containsEntry("centerCutJobId", "CPF_REF_CENTER_CUT_SAMPLE_JOB");
        verify(service).findJobs();
    }

    @Test
    void findTargetsAcceptsStatusAndLimitFilters() {
        when(service.findTargets("CPF_REF_CENTER_CUT_SAMPLE_JOB", "FAILED", 20))
                .thenReturn(List.of(Map.of(
                        "statusCode", "FAILED",
                        "parentTransactionGlobalId", "20260615120000000REFbatAP010000001",
                        "lastErrorMessage", "교육용 강제 실패")));

        ResponseEntity<List<Map<String, Object>>> response =
                controller.findTargets("CPF_REF_CENTER_CUT_SAMPLE_JOB", "FAILED", 20);

        assertThat(response.getBody()).singleElement().satisfies(item ->
                assertThat(item)
                        .containsEntry("statusCode", "FAILED")
                        .containsEntry("lastErrorMessage", "교육용 강제 실패"));
        verify(service).findTargets("CPF_REF_CENTER_CUT_SAMPLE_JOB", "FAILED", 20);
    }

    @Test
    void findResultDetailDoesNotRequireCenterCutJobId() {
        when(service.findResultDetail("1")).thenReturn(Map.of(
                "resultId", 1L,
                "resultPayloadMasked", "[MASKED result payload length=25]",
                "found", true));

        ResponseEntity<Map<String, Object>> response = controller.findResultDetail("1");

        assertThat(response.getBody())
                .containsEntry("found", true)
                .containsKey("resultPayloadMasked");
        verify(service).findResultDetail("1");
    }
}
