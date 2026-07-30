package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayLoadBalancePolicy;
import com.cpf.core.api.gateway.CpfGatewayTargetSelectionPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Gateway Runtime의 실제 Target Selector입니다. */
@Component
public final class CpfGatewayTargetSelector implements CpfGatewayTargetSelectionPort {
    private final Map<String, AtomicLong> sequences = new ConcurrentHashMap<>();

    @Override
    public SelectionResult select(SelectionRequest request) {
        if (request == null || request.policy() == null) {
            throw new IllegalArgumentException("Selection request and policy are required");
        }
        List<TargetCandidate> eligible = request.candidates().stream()
                .filter(TargetCandidate::routable)
                .filter(this::canaryEligible)
                .sorted(Comparator.comparing(TargetCandidate::instanceId))
                .toList();
        if (eligible.isEmpty()) {
            return SelectionResult.unavailable(request.serverGroupId(), request.policy(), "NO_ROUTABLE_INSTANCE");
        }
        TargetCandidate selected = switch (request.policy()) {
            case ROUND_ROBIN -> roundRobin(request.serverGroupId(), eligible);
            case WEIGHTED_ROUND_ROBIN -> weighted(request.serverGroupId(), eligible);
            case RENDEZVOUS_HASH -> rendezvous(request.affinityKey(), eligible);
            case PRIORITY_FAILOVER -> priority(request.serverGroupId(), eligible);
            case LEAST_LOAD -> leastLoad(eligible);
        };
        return new SelectionResult(request.serverGroupId(), selected.instanceId(), selected.host(), selected.port(),
                request.policy(), "SELECTED", eligible.size(), OffsetDateTime.now());
    }

    private TargetCandidate roundRobin(String groupId, List<TargetCandidate> candidates) {
        long index = sequences.computeIfAbsent(key(groupId, "rr"), ignored -> new AtomicLong()).getAndIncrement();
        return candidates.get(Math.floorMod(index, candidates.size()));
    }

    private TargetCandidate weighted(String groupId, List<TargetCandidate> candidates) {
        int total = candidates.stream().mapToInt(candidate -> Math.max(1, candidate.weight())).sum();
        long sequence = sequences.computeIfAbsent(key(groupId, "weighted"), ignored -> new AtomicLong()).getAndIncrement();
        int slot = Math.floorMod(sequence, total);
        for (TargetCandidate candidate : candidates) {
            slot -= Math.max(1, candidate.weight());
            if (slot < 0) return candidate;
        }
        return candidates.getLast();
    }

    private TargetCandidate priority(String groupId, List<TargetCandidate> candidates) {
        int bestPriority = candidates.stream().mapToInt(TargetCandidate::priority).min().orElse(0);
        List<TargetCandidate> tier = candidates.stream().filter(candidate -> candidate.priority() == bestPriority).toList();
        return weighted(key(groupId, "priority-" + bestPriority), tier);
    }

    private TargetCandidate leastLoad(List<TargetCandidate> candidates) {
        return candidates.stream().min(Comparator
                .comparingDouble((TargetCandidate candidate) -> loadScore(candidate))
                .thenComparing(TargetCandidate::instanceId)).orElseThrow();
    }

    private double loadScore(TargetCandidate candidate) {
        double latency = candidate.ewmaLatencyMs() <= 0 ? 1.0 : candidate.ewmaLatencyMs();
        return (candidate.activeRequests() + 1.0) * latency / Math.max(1, candidate.weight());
    }

    private TargetCandidate rendezvous(String affinityKey, List<TargetCandidate> candidates) {
        if (affinityKey == null || affinityKey.isBlank()) {
            throw new IllegalArgumentException("RENDEZVOUS_HASH requires a non-blank affinity key");
        }
        TargetCandidate selected = null;
        double selectedScore = Double.NEGATIVE_INFINITY;
        for (TargetCandidate candidate : candidates) {
            long hash = unsignedHash(affinityKey + '\u0000' + candidate.instanceId());
            double normalized = (hash & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
            double score = Math.log(Math.max(normalized, Double.MIN_VALUE)) / Math.max(1, candidate.weight());
            if (selected == null || score > selectedScore) {
                selected = candidate;
                selectedScore = score;
            }
        }
        return selected;
    }

    private long unsignedHash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            long result = 0;
            for (int i = 0; i < Long.BYTES; i++) result = (result << 8) | (digest[i] & 0xffL);
            return result;
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private boolean canaryEligible(TargetCandidate candidate) {
        if (candidate.canaryPercent() <= 0 || candidate.canaryPercent() >= 100) return true;
        long sequence = sequences.computeIfAbsent(key(candidate.instanceId(), "canary"), ignored -> new AtomicLong()).getAndIncrement();
        return Math.floorMod(sequence, 100) < candidate.canaryPercent();
    }

    private static String key(String groupId, String suffix) { return String.valueOf(groupId) + ':' + suffix; }
}
