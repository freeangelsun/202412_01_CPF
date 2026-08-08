package com.cpf.core.api.health;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface CpfRuntimeHealthRegistry {
    record Report(CpfRuntimeHealth health, Instant reportedAt) {}
    void report(CpfRuntimeHealth health, Instant reportedAt);
    Optional<Report> find(String systemId, String instanceId);
    List<Report> search(String systemId, String readiness, int offset, int limit);
    long count(String systemId, String readiness);
    int purgeBefore(Instant cutoff);
}
