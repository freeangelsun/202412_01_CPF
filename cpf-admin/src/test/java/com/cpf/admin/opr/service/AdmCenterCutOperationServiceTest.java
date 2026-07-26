package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ADM이 Center-Cut Owner DB 대신 공개 운영 Port에만 위임하는지 검증합니다. */
class AdmCenterCutOperationServiceTest {

    private final CpfCenterCutOperationsPort port = mock(CpfCenterCutOperationsPort.class);
    private final AdmCenterCutOperationService service = new AdmCenterCutOperationService(port);

    @Test
    void delegatesResultDetailToOwnerPortWithoutLocalPayloadHandling() {
        Map<String, Object> ownerResult = Map.of(
                "resultId", "RESULT-1",
                "resultPayloadMasked", "[MASKED by BAT owner]");
        when(port.findResultDetail("RESULT-1")).thenReturn(ownerResult);

        assertThat(service.findResultDetail("RESULT-1")).isSameAs(ownerResult);
        verify(port).findResultDetail("RESULT-1");
    }

    @Test
    void delegatesTargetFilterAndLimitToOwnerPort() {
        List<Map<String, Object>> ownerResult = List.of(Map.of("targetId", "TARGET-1"));
        when(port.findTargets("JOB-1", "WAITING", 50)).thenReturn(ownerResult);

        assertThat(service.findTargets("JOB-1", "WAITING", 50)).isSameAs(ownerResult);
        verify(port).findTargets("JOB-1", "WAITING", 50);
    }
}
