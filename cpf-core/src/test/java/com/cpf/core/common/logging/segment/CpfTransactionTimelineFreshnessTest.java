package com.cpf.core.common.logging.segment;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CpfTransactionTimelineFreshnessTest {
    @Test
    void pureLocalDoesNotBecomePartialBecauseRemoteMessageBatchFileOrDlqNeverApplied() {
        var result = CpfTransactionTimelineQueryFacade.classifySourceFreshness(
                "T001", List.of(Map.of("sourceType", "LOCAL", "freshnessAt", Instant.now())), Set.of());
        assertEquals(false, result.get("partial"));
        assertTrue(((List<?>) result.get("missingSources")).isEmpty());
        var na = (List<?>) result.get("notApplicableSources");
        assertTrue(na.contains("REMOTE"));
        assertTrue(na.contains("MESSAGE"));
        assertTrue(na.contains("DLQ"));
        assertTrue(na.contains("BATCH"));
        assertTrue(na.contains("FILE"));
    }

    @Test
    void observedRemoteBecomesApplicableWithoutChangingTransactionIdentity() {
        var now = Instant.now();
        var result = CpfTransactionTimelineQueryFacade.classifySourceFreshness(
                "T001", List.of(
                        Map.of("sourceType", "LOCAL", "freshnessAt", now),
                        Map.of("sourceType", "REMOTE", "freshnessAt", now)), Set.of());
        assertEquals(false, result.get("partial"));
        var sources = (List<Map<String,Object>>) result.get("sources");
        var remote = sources.stream().filter(v -> "REMOTE".equals(v.get("sourceType"))).findFirst().orElseThrow();
        assertEquals("APPLICABLE", remote.get("applicability"));
        assertEquals("AVAILABLE", remote.get("availability"));
    }

    @Test
    void failedApplicableSourceIsNotMisreportedAsNotApplicable() {
        var result = CpfTransactionTimelineQueryFacade.classifySourceFreshness("T001", List.of(), Set.of("LOCAL"));
        assertEquals(true, result.get("partial"));
        assertEquals(List.of("LOCAL"), result.get("failedSources"));
        assertFalse(((List<?>) result.get("notApplicableSources")).contains("LOCAL"));
    }

    @Test
    void queryFailureMarkerIsPartialAndNeverNotApplicable() {
        for (String source : List.of("REMOTE", "MESSAGE", "DLQ", "BATCH", "FILE", "TRACE", "AUDIT")) {
            var result = CpfTransactionTimelineQueryFacade.classifySourceFreshness(
                    "T001",
                    List.of(Map.of("sourceType", source, "queryState", "QUERY_FAILED", "failureStage", "RuntimeException")),
                    Set.of());
            assertEquals(true, result.get("partial"), source);
            assertTrue(((List<?>) result.get("failedSources")).contains(source), source);
            assertFalse(((List<?>) result.get("notApplicableSources")).contains(source), source);
        }
    }
}
