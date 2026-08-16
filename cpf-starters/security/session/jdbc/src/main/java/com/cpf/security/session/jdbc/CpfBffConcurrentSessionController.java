package com.cpf.security.session.jdbc;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

/**
 * Spring Session의 Principal Index를 이용해 ADM/BZA 동시 Session 상한을 다중 인스턴스에서 강제합니다.
 */
final class CpfBffConcurrentSessionController {
    private final FindByIndexNameSessionRepository<? extends Session> repository;

    CpfBffConcurrentSessionController(FindByIndexNameSessionRepository<? extends Session> repository) {
        this.repository = repository;
    }

    void register(HttpSession current, String principalId, int maximumSessions) {
        if (principalId == null || principalId.isBlank()) {
            throw new IllegalStateException("CPF_BFF_PRINCIPAL_REQUIRED");
        }
        if (maximumSessions < 1) {
            throw new IllegalArgumentException("maximumSessions must be positive");
        }

        Map<String, ? extends Session> existing = repository.findByPrincipalName(principalId);
        boolean currentAlreadyIndexed = existing.containsKey(current.getId());
        int projectedSessionCount = existing.size() + (currentAlreadyIndexed ? 0 : 1);
        int deleteCount = Math.max(0, projectedSessionCount - maximumSessions);
        existing.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(current.getId()))
                .sorted(Comparator
                        .comparing((Map.Entry<String, ? extends Session> entry) ->
                                value(entry.getValue().getLastAccessedTime()))
                        .thenComparing(Map.Entry::getKey))
                .limit(deleteCount)
                .forEach(entry -> repository.deleteById(entry.getKey()));

        current.setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                principalId);
    }

    private static Instant value(Instant instant) {
        return instant == null ? Instant.EPOCH : instant;
    }
}
