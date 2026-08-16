package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmPageResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdmServerPageServiceTest {
    private final AdmServerPageService service = new AdmServerPageService();

    @Test
    void filtersSortsAndPagesWithoutMutatingSource() {
        List<Map<String, Object>> source = List.of(
                Map.of("executionId", 3L, "jobId", "JOB-B", "status", "FAILED"),
                Map.of("executionId", 1L, "jobId", "JOB-A", "status", "COMPLETED"),
                Map.of("executionId", 2L, "jobId", "JOB-A", "status", "RUNNING"));
        AdmPageResponse<Map<String, Object>> response = service.page(
                source, 0, 1, "job-a", "executionId", "desc", Set.of("executionId"));
        assertEquals(2, response.total());
        assertTrue(response.hasNext());
        assertEquals(2L, response.items().getFirst().get("executionId"));
        assertEquals(3, source.size());
    }

    @Test
    void rejectsUnsafeSortAndOversizedPage() {
        assertThrows(IllegalArgumentException.class,
                () -> service.page(List.of(), 0, 10, "", "drop table", "asc", Set.of("id")));
        assertThrows(IllegalArgumentException.class,
                () -> service.page(List.of(), 0, 201, "", "id", "asc", Set.of("id")));
    }
}
