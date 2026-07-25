package com.cpf.batch.runtime.centercut;

import com.cpf.core.spi.centercut.CenterCutHandler;
import com.cpf.core.spi.centercut.CenterCutTargetProvider;

/**
 * BAT Center-Cut Runner에 등록되는 실행 정의입니다.
 *
 * @param jobId          안정적인 Center-Cut Job ID
 * @param provider       대상 조회/상태 반영 Owner Adapter
 * @param handler        업무 item 처리 Adapter
 * @param defaultLimit   기본 1회 처리 한도
 * @param maxLimit       운영자가 올릴 수 있는 절대 상한
 * @param ratePerSecond  초당 처리 허용량, 0 이하는 제한 없음
 */
public record BatCenterCutDefinition(
        String jobId,
        CenterCutTargetProvider provider,
        CenterCutHandler handler,
        int defaultLimit,
        int maxLimit,
        double ratePerSecond) {

    public BatCenterCutDefinition {
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId는 필수입니다.");
        if (provider == null) throw new IllegalArgumentException("provider는 필수입니다.");
        if (handler == null) throw new IllegalArgumentException("handler는 필수입니다.");
        defaultLimit = Math.max(1, defaultLimit);
        maxLimit = Math.max(defaultLimit, maxLimit);
        ratePerSecond = Math.max(0.0d, ratePerSecond);
    }
}
