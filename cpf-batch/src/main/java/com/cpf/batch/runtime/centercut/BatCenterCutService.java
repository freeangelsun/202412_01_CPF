package com.cpf.batch.runtime.centercut;

import com.cpf.core.spi.centercut.CenterCutHandler;
import com.cpf.core.spi.centercut.CenterCutTargetProvider;
import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutSummary;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.api.logging.CpfTransactionContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * BAT가 소유하는 center-cut 기본 실행 Runtime입니다.
 *
 * <p>Core에는 Target/Handler/Result 계약만 남기고 실제 Runner 실행은 BAT가 소유합니다.
 * 하나의 center-cut 업무 흐름은 transactionId 하나를 승계합니다. 각 item 실행은 별도
 * transactionSegmentId로 구분하며, parentSegmentId는 호출/배치 상위 실행 segment를 가리킵니다.</p>
 */
public class BatCenterCutService {
    private static final DateTimeFormatter SEGMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final Supplier<String> transactionSegmentIdSupplier;

    public BatCenterCutService() {
        this(new LocalCenterCutSegmentIdSupplier(Clock.systemDefaultZone()));
    }

    public BatCenterCutService(Supplier<String> transactionSegmentIdSupplier) {
        this.transactionSegmentIdSupplier = Objects.requireNonNull(
                transactionSegmentIdSupplier,
                "transactionSegmentIdSupplier");
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

        String executionTransactionId = CpfTransactionContext.transactionId();
        String executionParentSegmentId = CpfTransactionContext.currentSegmentId();

        int success = 0;
        int failed = 0;
        int skipped = 0;
        int retryRequested = 0;
        int unknownResult = 0;
        int stopRequested = 0;

        for (CpfCenterCutTarget target : targets) {
            String transactionId = firstText(target.transactionId(), executionTransactionId);
            String parentSegmentId = firstText(target.parentSegmentId(), executionParentSegmentId);
            String transactionSegmentId = nextTransactionSegmentId();

            CpfCenterCutTarget runningTarget = target
                    .withExecutionContext(transactionId, parentSegmentId, transactionSegmentId)
                    .withStatus(CpfCenterCutStatus.RUNNING);

            provider.markRunning(runningTarget);
            CpfCenterCutResult result = handleSafely(runningTarget, handler);
            provider.markResult(runningTarget, result);

            switch (result.status()) {
                case SUCCESS -> success++;
                case FAILED -> failed++;
                case SKIPPED -> skipped++;
                case RETRY_REQUESTED -> retryRequested++;
                case UNKNOWN_RESULT -> unknownResult++;
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
                unknownResult,
                stopRequested);
    }

    private CpfCenterCutResult handleSafely(
            CpfCenterCutTarget target,
            CenterCutHandler handler) {
        try {
            CpfCenterCutResult result = handler.handle(target);
            if (result == null) {
                return CpfCenterCutResult.failed(
                        target,
                        "center-cut handler가 결과를 반환하지 않았습니다.",
                        null);
            }
            if (result.transactionSegmentId() == null || result.transactionSegmentId().isBlank()) {
                return new CpfCenterCutResult(
                        result.targetId(),
                        result.status(),
                        result.message(),
                        result.resultPayload(),
                        target.transactionSegmentId());
            }
            return result;
        } catch (Exception ex) {
            return CpfCenterCutResult.failed(target, ex.getMessage(), null);
        }
    }

    private String nextTransactionSegmentId() {
        String generated = transactionSegmentIdSupplier.get();
        if (generated == null || generated.isBlank()) {
            throw new IllegalStateException("center-cut transactionSegmentId를 생성하지 못했습니다.");
        }
        if (generated.length() > 120) {
            throw new IllegalStateException("center-cut transactionSegmentId는 120자를 초과할 수 없습니다.");
        }
        return generated;
    }

    private static String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : second;
    }

    private static final class LocalCenterCutSegmentIdSupplier implements Supplier<String> {
        private final Clock clock;
        private final AtomicLong sequence = new AtomicLong();

        private LocalCenterCutSegmentIdSupplier(Clock clock) {
            this.clock = clock;
        }

        @Override
        public String get() {
            long next = sequence.incrementAndGet() % 10_000_000L;
            return "CC-"
                    + LocalDateTime.now(clock).format(SEGMENT_TIME_FORMAT)
                    + "-"
                    + String.format(Locale.ROOT, "%07d", next);
        }
    }
}
