package com.cpf.batch.runtime.centercut;

import com.cpf.core.common.servicecall.ServiceCallResolvedTarget;

/**
 * Generated Domain/분리 WAS의 Center-Cut Handler를 호출하는 실제 전송 SPI입니다.
 * HTTP/메시징 구현은 배포 토폴로지에 맞는 adapter가 제공하고, BAT는 ServiceCallEngine의
 * registry/retry/failover/unknown-result 정책만 공통으로 사용합니다.
 */
@FunctionalInterface
public interface BatCenterCutRemoteTransport {
    String exchange(ServiceCallResolvedTarget target, BatCenterCutRemoteRequest request);

    record BatCenterCutRemoteRequest(
            String targetId,
            String centerCutJobId,
            String businessKey,
            String businessDate,
            String payload,
            String transactionId,
            String parentSegmentId,
            String transactionSegmentId,
            int retryCount) {
    }
}
