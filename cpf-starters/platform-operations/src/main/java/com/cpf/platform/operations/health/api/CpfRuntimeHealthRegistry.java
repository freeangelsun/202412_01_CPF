package com.cpf.platform.operations.health.api;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
/** CpfRuntimeHealthRegistry 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfRuntimeHealthRegistry {
    record Report(CpfRuntimeHealth health, Instant reportedAt) {}
    void report(CpfRuntimeHealth health, Instant reportedAt);
    Optional<Report> find(String systemId, String instanceId);
    List<Report> search(String systemId, String readiness, int offset, int limit);
    long count(String systemId, String readiness);
    int purgeBefore(Instant cutoff);
}
