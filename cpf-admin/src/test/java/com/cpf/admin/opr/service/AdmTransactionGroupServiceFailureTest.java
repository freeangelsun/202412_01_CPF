package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.logging.CpfTransactionTimelineQueryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** ADM timeline이 적용 가능한 BATCH owner 조회 실패를 정상 empty로 숨기지 않는지 검증합니다. */
class AdmTransactionGroupServiceFailureTest {

    @Test
    void batchQueryFailureRemainsPartialAndOperatorVisible() {
        CpfTransactionTimelineQueryPort timeline = mock(CpfTransactionTimelineQueryPort.class);
        CpfBatchOperationsPort batch = mock(CpfBatchOperationsPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<CpfBatchOperationsPort> provider = mock(ObjectProvider.class);

        when(provider.getIfAvailable()).thenReturn(batch);
        when(timeline.findSegments("TX-BATCH-FAIL")).thenReturn(List.of());
        when(timeline.findExternalCandidates("TX-BATCH-FAIL", 100)).thenReturn(List.of());
        when(timeline.findLineage("TX-BATCH-FAIL", 500)).thenReturn(List.of());
        when(timeline.sourceFreshness("TX-BATCH-FAIL")).thenReturn(Map.of(
                "transactionId", "TX-BATCH-FAIL",
                "partial", false,
                "missingSources", List.of(),
                "failedSources", List.of(),
                "sources", List.of()));
        when(batch.findExecutions(isNull(), eq("TX-BATCH-FAIL"), isNull(), isNull(), isNull(), anyInt()))
                .thenThrow(new IllegalStateException("simulated batch owner unavailable"));

        AdmTransactionGroupService service = new AdmTransactionGroupService(timeline, provider);
        Map<String, Object> detail = service.findDetail("TX-BATCH-FAIL");

        @SuppressWarnings("unchecked")
        Map<String, Object> freshness = (Map<String, Object>) detail.get("sourceFreshness");
        assertThat(freshness.get("partial")).isEqualTo(true);
        assertThat(freshness.get("resultState")).isEqualTo("PARTIAL");
        assertThat((List<?>) freshness.get("missingSources")).contains("BATCH");
        assertThat((List<?>) freshness.get("failedSources")).contains("BATCH");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sources = (List<Map<String, Object>>) freshness.get("sources");
        Map<String, Object> batchSource = sources.stream()
                .filter(source -> "BATCH".equals(source.get("sourceType")))
                .findFirst()
                .orElseThrow();
        assertThat(batchSource).containsEntry("state", "QUERY_FAILED");
        assertThat(batchSource).containsEntry("availability", "UNAVAILABLE");
        assertThat(batchSource).containsEntry("reason", "BATCH_QUERY_FAILED");
        assertThat(batchSource).containsEntry("failureType", "IllegalStateException");
    }
}
