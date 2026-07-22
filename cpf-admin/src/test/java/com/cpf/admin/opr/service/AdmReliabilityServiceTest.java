package com.cpf.admin.opr.service;

import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmReliabilityServiceTest {

    @Test
    void delegatesReliabilityQueriesToCpfPublicPort() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        when(port.findOutbox("FAILED", "TX-1", "cpf.topic", 1000))
                .thenReturn(List.of(Map.of("messageId", "M1")));
        AdmReliabilityService service = new AdmReliabilityService(port);

        List<Map<String, Object>> result = service.findOutbox("FAILED", "TX-1", "cpf.topic", 1000);

        assertThat(result).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("messageId", "M1"));
        verify(port).findOutbox("FAILED", "TX-1", "cpf.topic", 1000);
    }

    @Test
    void mapsCpfChangeResultWithoutLosingAuditReason() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        when(port.resolveUnknown("U1", "CONFIRMED_SUCCESS", "OP1", "외부 결과 확인"))
                .thenReturn(new CpfReliabilityOperationsPort.ChangeResult(
                        Map.of("unknownStatus", "CHECK_PENDING"),
                        Map.of("unknownStatus", "CONFIRMED_SUCCESS"),
                        "외부 결과 확인"));
        AdmReliabilityService service = new AdmReliabilityService(port);

        AdmReliabilityService.ChangeResult result = service.resolveUnknown(
                "U1", "CONFIRMED_SUCCESS", "OP1", "외부 결과 확인");

        assertThat(result.before()).containsEntry("unknownStatus", "CHECK_PENDING");
        assertThat(result.after()).containsEntry("unknownStatus", "CONFIRMED_SUCCESS");
        assertThat(result.reason()).isEqualTo("외부 결과 확인");
    }
}
