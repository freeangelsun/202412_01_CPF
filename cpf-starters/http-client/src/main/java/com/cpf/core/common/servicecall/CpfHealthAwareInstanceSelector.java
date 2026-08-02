package com.cpf.core.common.servicecall;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 서비스 instance 목록에서 health/maintenance/drain/priority/zone/weight를 반영해 호출 대상을 선택합니다.
 *
 * <p>동일 routingKey에서는 weighted rendezvous hash를 사용해 안정적인 분산을 제공하고,
 * 대상 장애/제외 시 남은 후보로 자연스럽게 failover합니다. 명시 instance 요청도 운영 차단 상태면 선택하지 않습니다.</p>
 */
public class CpfHealthAwareInstanceSelector {

    public Optional<Map<String, Object>> select(List<Map<String, Object>> instances, String requestedInstanceId) {
        return select(instances, requestedInstanceId, Set.of(), SelectionContext.defaults());
    }

    public Optional<Map<String, Object>> select(
            List<Map<String, Object>> instances,
            String requestedInstanceId,
            Set<String> excludedInstanceIds) {
        return select(instances, requestedInstanceId, excludedInstanceIds, SelectionContext.defaults());
    }

    public Optional<Map<String, Object>> select(
            List<Map<String, Object>> instances,
            String requestedInstanceId,
            Set<String> excludedInstanceIds,
            SelectionContext context) {
        if (instances == null || instances.isEmpty()) return Optional.empty();
        SelectionContext effective = context == null ? SelectionContext.defaults() : context;

        List<Map<String, Object>> candidates = instances.stream()
                .filter(row -> !excluded(excludedInstanceIds, row))
                .filter(this::routable)
                .filter(row -> effective.includeMaintenance() || !yes(row.get("maintenanceYn")))
                .filter(row -> effective.includeDraining() || !yes(row.get("drainYn")))
                .toList();
        if (candidates.isEmpty()) return Optional.empty();

        if (requestedInstanceId != null && !requestedInstanceId.isBlank()) {
            return candidates.stream()
                    .filter(row -> requestedInstanceId.equals(text(row.get("instanceId"))))
                    .findFirst();
        }

        int bestPriority = candidates.stream().mapToInt(this::priority).min().orElse(0);
        List<Map<String, Object>> priorityCandidates = candidates.stream()
                .filter(row -> priority(row) == bestPriority)
                .toList();

        if (effective.preferredZone() != null && !effective.preferredZone().isBlank()) {
            List<Map<String, Object>> sameZone = priorityCandidates.stream()
                    .filter(row -> effective.preferredZone().equalsIgnoreCase(text(row.get("zoneCode"))))
                    .toList();
            if (!sameZone.isEmpty()) priorityCandidates = sameZone;
        }
        if (effective.preferredCell() != null && !effective.preferredCell().isBlank()) {
            List<Map<String, Object>> sameCell = priorityCandidates.stream()
                    .filter(row -> effective.preferredCell().equalsIgnoreCase(text(row.get("cellCode"))))
                    .toList();
            if (!sameCell.isEmpty()) priorityCandidates = sameCell;
        }

        String key = effective.routingKey();
        if (key == null || key.isBlank()) {
            key = Thread.currentThread().threadId() + ":" + System.nanoTime();
        }
        final String routingKey = key;
        return priorityCandidates.stream()
                .min(Comparator.comparingDouble(row -> rendezvousScore(routingKey, row)));
    }

    private boolean routable(Map<String, Object> row) {
        return yes(row.get("activeYn")) && "UP".equalsIgnoreCase(text(row.get("instanceStatus")));
    }

    private int priority(Map<String, Object> row) {
        Object value = first(row, "priorityNo", "priority", "priority_no");
        if (value == null) return 100;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return 100; }
    }

    private int weight(Map<String, Object> row) {
        Object value = row.get("weight");
        int result = 100;
        if (value instanceof Number n) result = n.intValue();
        else if (value != null) {
            try { result = Integer.parseInt(String.valueOf(value)); }
            catch (NumberFormatException ignored) { result = 100; }
        }
        return Math.max(1, Math.min(10_000, result));
    }

    /** Weighted rendezvous: 가장 작은 -ln(U)/weight가 선택됩니다. */
    private double rendezvousScore(String routingKey, Map<String, Object> row) {
        String instanceId = text(row.get("instanceId"));
        long hash = positiveHash64(routingKey + '\u0000' + instanceId);
        double u = (hash + 1.0d) / ((double) Long.MAX_VALUE + 2.0d);
        return -Math.log(u) / weight(row);
    }

    private long positiveHash64(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            long result = 0L;
            for (int i = 0; i < 8; i++) result = (result << 8) | (bytes[i] & 0xffL);
            return result & Long.MAX_VALUE;
        } catch (Exception ex) {
            throw new IllegalStateException("instance routing hash 생성에 실패했습니다.", ex);
        }
    }

    private boolean excluded(Set<String> excludedInstanceIds, Map<String, Object> row) {
        if (excludedInstanceIds == null || excludedInstanceIds.isEmpty()) return false;
        String instanceId = text(row.get("instanceId"));
        return !instanceId.isBlank() && excludedInstanceIds.contains(instanceId);
    }

    private boolean yes(Object value) { return "Y".equalsIgnoreCase(text(value)); }
    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private Object first(Map<String, Object> row, String... keys) {
        for (String key : keys) if (row.containsKey(key) && row.get(key) != null) return row.get(key);
        return null;
    }

    public record SelectionContext(
            String routingKey,
            String preferredZone,
            String preferredCell,
            boolean includeDraining,
            boolean includeMaintenance) {
        public static SelectionContext defaults() { return new SelectionContext(null, null, null, false, false); }
    }
}
