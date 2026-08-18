package com.cpf.data.persistence.api.transaction;

import java.util.function.Consumer;

/**
 * Local DB transaction lifecycle을 업무 코드에서 raw Spring synchronization API 없이 사용하는 공개 계약입니다.
 *
 * <p>{@code afterCommit}은 실제 commit 성공 이후, {@code afterRollback}은 실제 rollback 완료 이후,
 * {@code afterCompletion}은 Transaction Manager가 completion 상태를 확정한 뒤 실행됩니다. 등록 시점에
 * active transaction이 없으면 fail-fast 합니다. 장시간 작업이나 전달보장이 필요한 작업은 이 hook에서
 * 직접 수행하지 말고 Outbox/Messaging/Worker를 사용해야 합니다.</p>
 *
 * <p>{@link #setRollbackOnly()}는 일반 업무 흐름의 기본 제어 방식이 아닙니다. 일반 업무 실패는
 * {@code @CpfTransactional + Exception + rollback rules}를 사용하고, 이 메서드는 명시적 rollback mark가
 * 필요한 고급 integration 상황에만 사용합니다.</p>
 */
public interface CpfTransactionOperations {

    /** 현재 Thread에 실제 Local DB transaction이 active인지 반환합니다. */
    boolean isActive();

    /** 현재 transaction이 rollback-only로 표시됐는지 반환합니다. active transaction이 없으면 실패합니다. */
    boolean isRollbackOnly();

    /** 현재 transaction을 rollback-only로 표시합니다. 일반 업무 Golden Path가 아닌 고급 Escape Hatch입니다. */
    void setRollbackOnly();

    /** 실제 Local DB commit 성공 이후 callback을 실행하도록 등록합니다. */
    void afterCommit(Runnable callback);

    /** 실제 Local DB commit 성공 이후 지정 order로 callback을 실행하도록 등록합니다. */
    void afterCommit(int order, Runnable callback);

    /** 실제 Local DB rollback 완료 이후 callback을 실행하도록 등록합니다. */
    void afterRollback(Runnable callback);

    /** 실제 Local DB rollback 완료 이후 지정 order로 callback을 실행하도록 등록합니다. */
    void afterRollback(int order, Runnable callback);

    /** Local DB transaction completion 이후 최종 상태를 callback에 전달합니다. */
    void afterCompletion(Consumer<CompletionStatus> callback);

    /** Local DB transaction completion 이후 지정 order로 최종 상태를 callback에 전달합니다. */
    void afterCompletion(int order, Consumer<CompletionStatus> callback);

    /** Spring transaction completion 의미를 CPF Public Surface에 필요한 최소 범위로 노출합니다. */
    enum CompletionStatus {
        COMMITTED,
        ROLLED_BACK,
        UNKNOWN
    }
}
