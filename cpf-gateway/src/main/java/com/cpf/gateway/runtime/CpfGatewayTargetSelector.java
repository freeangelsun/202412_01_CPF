package com.cpf.gateway.runtime;

import com.cpf.gateway.api.CpfGatewayLoadBalancePolicy;
import com.cpf.gateway.api.CpfGatewayTargetSelectionPort;
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
        String requestKey = requiresDeterministicKey(request.candidates()) ? requestKey(request) : "";
        List<TargetCandidate> eligible = selectCanaryPool(request.serverGroupId(), requestKey, request.candidates());
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

    private List<TargetCandidate> selectCanaryPool(
            String groupId, String requestKey, List<TargetCandidate> candidates) {
        List<TargetCandidate> configured = candidates.stream()
                .sorted(Comparator.comparing(TargetCandidate::instanceId))
                .toList();
        List<TargetCandidate> stable = configured.stream()
                .filter(candidate -> candidate.canaryPercent() == 0)
                .filter(TargetCandidate::routable)
                .toList();
        List<TargetCandidate> canaries = configured.stream()
                .filter(candidate -> candidate.canaryPercent() > 0)
                .toList();
        if (canaries.isEmpty()) return stable;

        int totalPercent = canaries.stream().mapToInt(TargetCandidate::canaryPercent).sum();
        if (totalPercent > 100) {
            throw new IllegalArgumentException("Canary traffic percent sum must not exceed 100: " + totalPercent);
        }
        int bucket = (int) Math.floorMod(unsignedHash(String.valueOf(groupId) + '\u0000' + requestKey), 100);
        int boundary = 0;
        for (TargetCandidate candidate : canaries) {
            boundary += candidate.canaryPercent();
            if (bucket < boundary) {
                if (candidate.routable()) return List.of(candidate);
                return stable;
            }
        }
        if (!stable.isEmpty()) return stable;
        return canaries.stream().filter(TargetCandidate::routable).toList();
    }

    private static boolean requiresDeterministicKey(List<TargetCandidate> candidates) {
        return candidates.stream().anyMatch(candidate ->
                candidate.canaryPercent() > 0 && candidate.canaryPercent() < 100);
    }

    private static String requestKey(SelectionRequest request) {
        if (request.affinityKey() != null && !request.affinityKey().isBlank()) {
            return request.affinityKey().trim();
        }
        for (String name : List.of("requestKey", "transactionId", "traceId", "idempotencyKey")) {
            String value = request.attributes().get(name);
            if (value != null && !value.isBlank()) return value.trim();
        }
        throw new IllegalArgumentException(
                "Canary selection requires affinityKey or requestKey/transactionId/traceId/idempotencyKey attribute");
    }

    private static String key(String groupId, String suffix) { return String.valueOf(groupId) + ':' + suffix; }
}
