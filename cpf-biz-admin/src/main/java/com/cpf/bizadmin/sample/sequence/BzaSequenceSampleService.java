package com.cpf.bizadmin.sample.sequence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 선택형 BZA 업무 채번 Customization Sample.
 *
 * <p>Spring Bean이나 기본 Runtime 상태를 만들지 않습니다. 고객 업무가 자신의 DB transaction 안에서
 * 현재 {@link SequenceState}를 잠그고 이 순수 함수를 호출한 뒤 반환 상태와 감사 항목을 함께 저장하는
 * 방식을 교육합니다. 승인 ID·사유·operationId·expectedVersion을 강제하며 동일 operationId 재호출은
 * 동일 request hash일 때만 같은 결과를 반환합니다.</p>
 */
public final class BzaSequenceSampleService {
    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_-]{1,31}");
    private static final Pattern PREFIX = Pattern.compile("[A-Z0-9_-]{0,16}");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public SequenceResult next(SequenceRule rule, SequenceState current, SequenceRequest request, Instant now) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        validate(rule, current, request);

        String requestHash = hash(request.ruleCode() + "|" + request.businessDate() + "|"
                + request.expectedVersion() + "|" + request.approvalId() + "|" + request.reason());
        if (request.operationId().equals(current.lastOperationId())) {
            if (!requestHash.equals(current.lastRequestHash())) {
                throw new IllegalArgumentException("operationId가 다른 채번 요청에 이미 사용되었습니다.");
            }
            return new SequenceResult(current.lastGeneratedValue(), current, current.lastAudit(), true);
        }
        if (request.expectedVersion() != current.version()) {
            throw new IllegalStateException("채번 상태 version 충돌: expected=" + request.expectedVersion()
                    + ", actual=" + current.version());
        }

        long previous = request.businessDate().equals(current.businessDate()) ? current.currentValue() : 0L;
        long next = Math.addExact(previous, 1L);
        if (next > rule.maxValue()) {
            throw new IllegalStateException("채번 최대값을 초과했습니다.");
        }
        String value = rule.prefix() + DATE.format(request.businessDate())
                + String.format("%0" + rule.padding() + "d", next);
        SequenceAudit audit = new SequenceAudit(
                request.operatorId(), request.reason(), request.approvalId(), request.operationId(),
                current.version(), current.version() + 1, previous, next, value, now);
        SequenceState updated = new SequenceState(
                request.businessDate(), next, current.version() + 1,
                request.operationId(), requestHash, value, audit);
        return new SequenceResult(value, updated, audit, false);
    }

    public boolean reconcile(SequenceState persisted, SequenceResult result) {
        return persisted != null && result != null
                && persisted.version() == result.state().version()
                && Objects.equals(persisted.lastOperationId(), result.state().lastOperationId())
                && Objects.equals(persisted.lastGeneratedValue(), result.value());
    }

    private static void validate(SequenceRule rule, SequenceState current, SequenceRequest request) {
        if (!CODE.matcher(required(rule.ruleCode(), "ruleCode")).matches()) {
            throw new IllegalArgumentException("ruleCode 형식이 올바르지 않습니다.");
        }
        if (!PREFIX.matcher(Objects.requireNonNullElse(rule.prefix(), "")).matches()) {
            throw new IllegalArgumentException("prefix 형식이 올바르지 않습니다.");
        }
        if (rule.padding() < 1 || rule.padding() > 18) {
            throw new IllegalArgumentException("padding은 1~18이어야 합니다.");
        }
        if (rule.maxValue() < 1) throw new IllegalArgumentException("maxValue는 1 이상이어야 합니다.");
        if (!rule.ruleCode().equals(required(request.ruleCode(), "request.ruleCode"))) {
            throw new IllegalArgumentException("요청 ruleCode가 규칙과 다릅니다.");
        }
        Objects.requireNonNull(request.businessDate(), "businessDate");
        required(request.operationId(), "operationId");
        required(request.operatorId(), "operatorId");
        required(request.reason(), "reason");
        if (request.approvalId() < 1) throw new IllegalArgumentException("approvalId가 필요합니다.");
        if (current.version() < 0 || current.currentValue() < 0) {
            throw new IllegalArgumentException("현재 채번 상태가 올바르지 않습니다.");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + "는 필수입니다.");
        return value.trim();
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record SequenceRule(String ruleCode, String prefix, int padding, long maxValue) {}
    public record SequenceRequest(String ruleCode, LocalDate businessDate, long expectedVersion,
                                  long approvalId, String operationId, String operatorId, String reason) {}
    public record SequenceAudit(String operatorId, String reason, long approvalId, String operationId,
                                long beforeVersion, long afterVersion, long beforeValue, long afterValue,
                                String generatedValue, Instant generatedAt) {}
    public record SequenceState(LocalDate businessDate, long currentValue, long version,
                                String lastOperationId, String lastRequestHash,
                                String lastGeneratedValue, SequenceAudit lastAudit) {
        public static SequenceState empty() {
            return new SequenceState(null, 0L, 0L, null, null, null, null);
        }
    }
    public record SequenceResult(String value, SequenceState state, SequenceAudit audit, boolean replay) {}
}
