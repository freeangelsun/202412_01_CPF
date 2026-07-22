package cpf.pfw.common.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * PFW 표준 멱등 실행 엔진입니다.
 *
 * <p>DB unique key를 최종 동시성 경계로 사용하고, 동일 요청의 성공 응답은 재사용합니다.
 * 같은 key에 다른 요청이 들어오거나 기존 처리가 진행 중이면 실행하지 않습니다.</p>
 */
public class CpfIdempotencyEngine {
    private final CpfIdempotencyPort port;
    private final Clock clock;

    public CpfIdempotencyEngine(CpfIdempotencyPort port) {
        this(port, Clock.systemUTC());
    }

    CpfIdempotencyEngine(CpfIdempotencyPort port, Clock clock) {
        this.port = Objects.requireNonNull(port, "port는 필수입니다.");
        this.clock = Objects.requireNonNull(clock, "clock은 필수입니다.");
    }

    public CpfIdempotencyExecutionResult execute(CpfIdempotencyCommand command, Supplier<String> operation) {
        Objects.requireNonNull(command, "command는 필수입니다.");
        Objects.requireNonNull(operation, "operation은 필수입니다.");

        Instant now = clock.instant();
        Instant expiresAt = now.plus(command.ttl());
        CpfIdempotencyRecord processing = new CpfIdempotencyRecord(
                command.scope(),
                command.idempotencyKey(),
                command.requestHash(),
                command.payloadHash(),
                CpfIdempotencyStatus.PROCESSING.name(),
                null,
                false,
                now,
                null,
                expiresAt);

        if (!port.reserve(processing)) {
            CpfIdempotencyRecord existing = port.find(command.scope(), command.idempotencyKey())
                    .orElseThrow(() -> new CpfIdempotencyException(
                            "CPF-IDEMPOTENCY-RACE",
                            "중복 key가 감지됐지만 기존 처리 기록을 조회하지 못했습니다."));
            return handleExisting(command, existing, expiresAt, operation);
        }
        return invoke(command, operation);
    }

    private CpfIdempotencyExecutionResult handleExisting(
            CpfIdempotencyCommand command,
            CpfIdempotencyRecord existing,
            Instant expiresAt,
            Supplier<String> operation) {
        if (!existing.sameRequest(command.requestHash())
                || !Objects.equals(existing.payloadHash(), command.payloadHash())) {
            throw new CpfIdempotencyException(
                    "CPF-IDEMPOTENCY-PAYLOAD-CONFLICT",
                    "같은 멱등 key에 다른 요청 내용이 사용됐습니다.");
        }

        CpfIdempotencyStatus status = CpfIdempotencyStatus.from(existing.status());
        if (status == CpfIdempotencyStatus.SUCCESS) {
            return result(command, status.name(), existing.storedResponse(), true);
        }
        if (status == CpfIdempotencyStatus.PROCESSING && !isExpired(existing)) {
            throw new CpfIdempotencyException(
                    "CPF-IDEMPOTENCY-IN-PROGRESS",
                    "동일 요청이 처리 중입니다.");
        }

        boolean restartAllowed = status == CpfIdempotencyStatus.EXPIRED
                || isExpired(existing)
                || ((status == CpfIdempotencyStatus.FAILED || status == CpfIdempotencyStatus.UNKNOWN)
                && existing.retryAllowed());
        if (!restartAllowed || !port.restart(
                command.scope(),
                command.idempotencyKey(),
                command.requestHash(),
                command.payloadHash(),
                clock.instant(),
                expiresAt)) {
            throw new CpfIdempotencyException(
                    "CPF-IDEMPOTENCY-RETRY-NOT-ALLOWED",
                    "기존 처리 상태에서는 재시도할 수 없습니다.");
        }
        return invoke(command, operation);
    }

    private CpfIdempotencyExecutionResult invoke(CpfIdempotencyCommand command, Supplier<String> operation) {
        try {
            String response = operation.get();
            port.complete(
                    command.scope(),
                    command.idempotencyKey(),
                    CpfIdempotencyStatus.SUCCESS.name(),
                    response,
                    false);
            return result(command, CpfIdempotencyStatus.SUCCESS.name(), response, false);
        } catch (CpfUnknownResultException ex) {
            port.complete(
                    command.scope(),
                    command.idempotencyKey(),
                    CpfIdempotencyStatus.UNKNOWN.name(),
                    null,
                    true);
            throw ex;
        } catch (RuntimeException ex) {
            port.complete(
                    command.scope(),
                    command.idempotencyKey(),
                    CpfIdempotencyStatus.FAILED.name(),
                    null,
                    true);
            throw ex;
        }
    }

    private boolean isExpired(CpfIdempotencyRecord record) {
        return record.expiresAt() != null && !record.expiresAt().isAfter(clock.instant());
    }

    private CpfIdempotencyExecutionResult result(
            CpfIdempotencyCommand command,
            String status,
            String response,
            boolean replayed) {
        return new CpfIdempotencyExecutionResult(
                status,
                response,
                replayed,
                command.transactionGlobalId(),
                command.segmentId());
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 hash를 계산할 수 없습니다.", ex);
        }
    }
}
