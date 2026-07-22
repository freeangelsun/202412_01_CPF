package cpf.pfw.common.batch.centercut;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * center-cut 대상 조회부터 결과 반영까지의 표준 실행 흐름을 제공합니다.
 */
public class CpfCenterCutService {
    private static final DateTimeFormatter CHILD_ID_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final Supplier<String> childTransactionIdSupplier;

    public CpfCenterCutService() {
        this(new LocalChildTransactionIdSupplier(Clock.systemDefaultZone()));
    }

    public CpfCenterCutService(Supplier<String> childTransactionIdSupplier) {
        this.childTransactionIdSupplier = Objects.requireNonNull(childTransactionIdSupplier, "childTransactionIdSupplier");
    }

    public CpfCenterCutSummary execute(
            String centerCutJobId,
            int limit,
            CenterCutTargetProvider provider,
            CenterCutHandler handler) {
        if (centerCutJobId == null || centerCutJobId.isBlank()) {
            throw new IllegalArgumentException("centerCutJobId는 필수입니다.");
        }
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(handler, "handler");

        int safeLimit = Math.max(1, limit);
        List<CpfCenterCutTarget> targets = provider.findReadyTargets(centerCutJobId, safeLimit);
        if (targets == null || targets.isEmpty()) {
            return CpfCenterCutSummary.empty(centerCutJobId);
        }

        int success = 0;
        int failed = 0;
        int skipped = 0;
        int retryRequested = 0;
        int stopRequested = 0;

        for (CpfCenterCutTarget target : targets) {
            String childTransactionGlobalId = resolveChildTransactionGlobalId(target);
            CpfCenterCutTarget runningTarget = target.withChildTransactionGlobalId(childTransactionGlobalId);
            provider.markRunning(runningTarget, childTransactionGlobalId);
            CpfCenterCutResult result = handleSafely(runningTarget, handler, childTransactionGlobalId);
            provider.markResult(runningTarget, result);

            switch (result.status()) {
                case SUCCESS -> success++;
                case FAILED -> failed++;
                case SKIPPED -> skipped++;
                case RETRY_REQUESTED -> retryRequested++;
                case STOP_REQUESTED -> stopRequested++;
                default -> failed++;
            }
        }

        return new CpfCenterCutSummary(
                centerCutJobId,
                targets.size(),
                success,
                failed,
                skipped,
                retryRequested,
                stopRequested);
    }

    private CpfCenterCutResult handleSafely(
            CpfCenterCutTarget target,
            CenterCutHandler handler,
            String childTransactionGlobalId) {
        try {
            CpfCenterCutResult result = handler.handle(target);
            if (result == null) {
                return CpfCenterCutResult.failed(target, "center-cut handler가 결과를 반환하지 않았습니다.", null, childTransactionGlobalId);
            }
            if (result.childTransactionGlobalId() == null || result.childTransactionGlobalId().isBlank()) {
                return new CpfCenterCutResult(
                        result.targetId(),
                        result.status(),
                        result.message(),
                        result.resultPayload(),
                        childTransactionGlobalId);
            }
            return result;
        } catch (Exception ex) {
            return CpfCenterCutResult.failed(target, ex.getMessage(), null, childTransactionGlobalId);
        }
    }

    private String resolveChildTransactionGlobalId(CpfCenterCutTarget target) {
        if (target.childTransactionGlobalId() != null && !target.childTransactionGlobalId().isBlank()) {
            return target.childTransactionGlobalId();
        }
        String generated = childTransactionIdSupplier.get();
        if (generated == null || generated.isBlank()) {
            throw new IllegalStateException("center-cut 자식 거래 ID를 생성하지 못했습니다.");
        }
        return generated;
    }

    private static final class LocalChildTransactionIdSupplier implements Supplier<String> {
        private final Clock clock;
        private final AtomicLong sequence = new AtomicLong();

        private LocalChildTransactionIdSupplier(Clock clock) {
            this.clock = clock;
        }

        @Override
        public String get() {
            long next = sequence.incrementAndGet() % 10_000_000L;
            return LocalDateTime.now(clock).format(CHILD_ID_TIME_FORMAT)
                    + "BAT"
                    + "centcut"
                    + String.format(Locale.ROOT, "%07d", next);
        }
    }
}
