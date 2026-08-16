package com.cpf.education.web.gateway;
import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;

import java.util.Objects;

/**
 * Gateway Entry Public API의 운영 조회 사용법을 보여 주는 교육용 Consumer입니다.
 *
 * <p>업무 Controller가 진입 정책을 다시 판정하거나 우회하지 않습니다. 실제 요청 차단은
 * {@code CpfScgPrimaryHandler}가 단일 Owner로 수행하며, 이 예제는 상태와 비식별 Telemetry만
 * 읽습니다. Remote address, path, credential과 원문 payload는 반환하지 않습니다.</p>
 */
public final class EducationGatewayEntryEducationSample {
    private final CpfGatewayEntryPolicyPort entryPolicy;

    public EducationGatewayEntryEducationSample(CpfGatewayEntryPolicyPort entryPolicy) {
        this.entryPolicy = Objects.requireNonNull(entryPolicy, "entryPolicy");
    }

    /** status 작업을 CPF 표준 계약에 따라 수행한다. */
    public EntryStatus status() {
        CpfGatewayEntryPolicyPort.Snapshot snapshot = entryPolicy.snapshot();
        CpfGatewayEntryPolicyPort.Telemetry telemetry = entryPolicy.telemetry();
        return new EntryStatus(
                snapshot.version(),
                snapshot.state().name(),
                snapshot.retryAfter().toSeconds(),
                telemetry.allowed(),
                telemetry.denied(),
                telemetry.observedAt().toString());
    }

    /** EntryStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record EntryStatus(
            long version,
            String state,
            long retryAfterSeconds,
            long allowed,
            long denied,
            String observedAt) {
    }
}
