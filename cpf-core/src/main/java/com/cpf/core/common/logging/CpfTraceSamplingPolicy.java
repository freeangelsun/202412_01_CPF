package com.cpf.core.common.logging;

import com.cpf.core.api.logging.DynamicLogLevelRule;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 거래 상세 Trace의 Runtime sampling 정책입니다.
 *
 * <p>거래 기본 로그는 이 정책으로 제거하지 않습니다. Sampling은 상세 trace 필드와
 * 진단 payload의 보존 범위만 제어합니다. 실패 거래와 동적 진단 거래는 항상 sampling합니다.</p>
 */
public final class CpfTraceSamplingPolicy {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.defaults());

    public Snapshot replace(
            long version,
            double defaultRate,
            Map<String, Double> moduleRates,
            Map<String, Double> businessTransactionRates,
            boolean alwaysSampleErrors) {
        Snapshot next = Snapshot.create(version, defaultRate, moduleRates, businessTransactionRates, alwaysSampleErrors);
        snapshot.set(next);
        return next;
    }

    public Snapshot current() {
        return snapshot.get();
    }

    public boolean shouldSample(
            String transactionId,
            String businessTransactionId,
            String moduleId,
            boolean success,
            DynamicLogLevelRule dynamicRule) {
        Snapshot current = snapshot.get();
        if (!success && current.alwaysSampleErrors()) return true;
        if (dynamicRule != null) return true;
        double rate = current.rateFor(moduleId, businessTransactionId);
        if (rate >= 1.0d) return true;
        if (rate <= 0.0d) return false;
        String stableKey = text(transactionId) + '|' + text(businessTransactionId) + '|' + text(moduleId);
        return deterministicUnit(stableKey) < rate;
    }

    private double deterministicUnit(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            long positive = ((long) (hash[0] & 0xff) << 48)
                    | ((long) (hash[1] & 0xff) << 40)
                    | ((long) (hash[2] & 0xff) << 32)
                    | ((long) (hash[3] & 0xff) << 24)
                    | ((long) (hash[4] & 0xff) << 16)
                    | ((long) (hash[5] & 0xff) << 8)
                    | ((long) (hash[6] & 0xff));
            return positive / (double) (1L << 56);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    public record Snapshot(
            long version,
            double defaultRate,
            Map<String, Double> moduleRates,
            Map<String, Double> businessTransactionRates,
            boolean alwaysSampleErrors) {

        private static Snapshot defaults() {
            return create(0L, 1.0d, Map.of(), Map.of(), true);
        }

        private static Snapshot create(
                long version,
                double defaultRate,
                Map<String, Double> moduleRates,
                Map<String, Double> businessTransactionRates,
                boolean alwaysSampleErrors) {
            if (version < 0) throw new IllegalArgumentException("version은 0 이상이어야 합니다.");
            LinkedHashMap<String, Double> modules = normalizeRates(moduleRates);
            LinkedHashMap<String, Double> transactions = normalizeRates(businessTransactionRates);
            return new Snapshot(
                    version,
                    normalizeRate(defaultRate),
                    Map.copyOf(modules),
                    Map.copyOf(transactions),
                    alwaysSampleErrors);
        }

        private double rateFor(String moduleId, String businessTransactionId) {
            String transactionKey = normalizeKey(businessTransactionId);
            if (transactionKey != null && businessTransactionRates.containsKey(transactionKey)) {
                return businessTransactionRates.get(transactionKey);
            }
            String moduleKey = normalizeKey(moduleId);
            if (moduleKey != null && moduleRates.containsKey(moduleKey)) {
                return moduleRates.get(moduleKey);
            }
            return defaultRate;
        }

        private static LinkedHashMap<String, Double> normalizeRates(Map<String, Double> source) {
            LinkedHashMap<String, Double> result = new LinkedHashMap<>();
            if (source == null) return result;
            source.forEach((key, value) -> {
                String normalized = normalizeKey(key);
                if (normalized != null && value != null) result.put(normalized, normalizeRate(value));
            });
            return result;
        }

        private static String normalizeKey(String value) {
            return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
        }

        private static double normalizeRate(double value) {
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0d || value > 1.0d) {
                throw new IllegalArgumentException("sampling rate는 0.0~1.0이어야 합니다.");
            }
            return value;
        }
    }
}
