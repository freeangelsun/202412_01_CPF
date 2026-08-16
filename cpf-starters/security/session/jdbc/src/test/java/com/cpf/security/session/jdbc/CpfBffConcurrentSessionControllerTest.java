package com.cpf.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;

class CpfBffConcurrentSessionControllerTest {
    @SuppressWarnings("unchecked")
    private final FindByIndexNameSessionRepository<MapSession> repository =
            mock(FindByIndexNameSessionRepository.class);
    private final CpfBffConcurrentSessionController controller =
            new CpfBffConcurrentSessionController(repository);

    @Test
    void evictsOldestSessionWhenLoginWouldExceedLimit() {
        HttpSession current = mock(HttpSession.class);
        when(current.getId()).thenReturn("current");
        MapSession oldest = session("oldest", Instant.parse("2026-07-31T00:00:00Z"));
        MapSession newest = session("newest", Instant.parse("2026-07-31T01:00:00Z"));
        Map<String, MapSession> sessions = new LinkedHashMap<>();
        sessions.put(oldest.getId(), oldest);
        sessions.put(newest.getId(), newest);
        when(repository.findByPrincipalName("ADM001")).thenReturn(sessions);

        controller.register(current, "ADM001", 2);

        verify(repository).deleteById("oldest");
        verify(current).setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                "ADM001");
    }

    @Test
    void refreshOfAlreadyIndexedSessionDoesNotEvictPeerAtLimit() {
        HttpSession current = mock(HttpSession.class);
        when(current.getId()).thenReturn("current");
        Map<String, MapSession> sessions = Map.of(
                "current", session("current", Instant.parse("2026-07-31T00:00:00Z")),
                "peer", session("peer", Instant.parse("2026-07-31T01:00:00Z")));
        when(repository.findByPrincipalName("ADM001")).thenReturn(sessions);

        controller.register(current, "ADM001", 2);

        verify(repository, never()).deleteById("peer");
        verify(repository, never()).deleteById("current");
    }

    private static MapSession session(String id, Instant lastAccessedAt) {
        MapSession session = new MapSession(id);
        session.setCreationTime(lastAccessedAt.minus(Duration.ofMinutes(5)));
        session.setLastAccessedTime(lastAccessedAt);
        return session;
    }
}
