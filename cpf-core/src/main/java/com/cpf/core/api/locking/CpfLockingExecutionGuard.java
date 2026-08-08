package com.cpf.core.api.locking;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** 유효한 fencing token을 보유한 동안에만 임계 구역을 실행하는 CPF 실행 Guard입니다. */
public final class CpfLockingExecutionGuard {
    private final CpfLockManager manager;

    /**
     * lock 관리 계약을 사용해 fencing 기반 실행 Guard를 생성합니다.
     * @param manager lease와 fencing token을 관리하는 Lock Manager
     * @throws NullPointerException manager가 {@code null}인 경우
     */
    public CpfLockingExecutionGuard(CpfLockManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    /**
     * lock 획득·사전/사후 fencing 검증·해제를 포함해 임계 작업을 실행합니다.
     * @param key lock 자원 키
     * @param ownerId 실행 소유자 식별자
     * @param requestId 멱등 요청 식별자
     * @param lease lease 유효시간
     * @param action fencing token을 직접 사용하지 않는 임계 작업
     * @param <T> 작업 반환 타입
     * @return 임계 작업 실행 결과
     */
    public <T> T execute(
            String key, String ownerId, String requestId, Duration lease, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        return executeFenced(key, ownerId, requestId, lease, ignored -> action.get());
    }

    /**
     * 저장소 commit 시 stale writer를 거부할 수 있도록 fencing token을 작업에 전달합니다.
     * @param key lock 자원 키
     * @param ownerId 실행 소유자 식별자
     * @param requestId 멱등 요청 식별자
     * @param lease lease 유효시간
     * @param action 발급된 lock token을 사용하는 임계 작업
     * @param <T> 작업 반환 타입
     * @return 사전·사후 fencing 검증을 통과한 작업 결과
     */
    public <T> T executeFenced(
            String key,
            String ownerId,
            String requestId,
            Duration lease,
            Function<CpfLockManager.LockToken, T> action) {
        Objects.requireNonNull(action, "action");
        CpfLockManager.AcquireResult acquired = manager.acquire(key, ownerId, requestId, lease);
        if (acquired.status() != CpfLockManager.AcquireStatus.ACQUIRED) {
            throw new LockUnavailableException(key, acquired.status(), acquired.reason());
        }
        CpfLockManager.LockToken token = acquired.token();
        Throwable actionFailure = null;
        try {
            requireValidFence(token);
            T result = action.apply(token);
            requireValidFence(token);
            return result;
        } catch (Throwable failure) {
            actionFailure = failure;
            throw failure;
        } finally {
            CpfLockManager.ReleaseResult released = manager.release(token, "EXECUTION_COMPLETED");
            if (released.status() != CpfLockManager.ReleaseStatus.RELEASED
                    && released.status() != CpfLockManager.ReleaseStatus.IDEMPOTENT_REPLAY) {
                LockReleaseException releaseFailure = new LockReleaseException(
                        key, token.fencingToken(), released.status(), released.reason());
                if (actionFailure != null) actionFailure.addSuppressed(releaseFailure);
                else throw releaseFailure;
            }
        }
    }

    private void requireValidFence(CpfLockManager.LockToken token) {
        if (!manager.validateToken(token)) {
            throw new StaleFenceException(token.key(), token.fencingToken());
        }
    }

    public static final class LockUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final CpfLockManager.AcquireStatus status;

        public LockUnavailableException(
                String key, CpfLockManager.AcquireStatus status, String reason) {
            super("Lock unavailable: " + key + " [" + status + "] (" + reason + ")");
            this.status = status;
        }

        /**
         * lock 획득 실패 상태를 반환합니다.
         * @return 획득 실패 상태
         */
        public CpfLockManager.AcquireStatus status() { return status; }
    }

    public static final class StaleFenceException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public StaleFenceException(String key, long token) {
            super("Stale fencing token: " + key + "/" + token);
        }
    }

    public static final class LockReleaseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final CpfLockManager.ReleaseStatus status;

        public LockReleaseException(
                String key, long token, CpfLockManager.ReleaseStatus status, String reason) {
            super("Lock release is not confirmed: " + key + "/" + token
                    + " [" + status + "] (" + reason + ")");
            this.status = status;
        }

        /**
         * lock 해제 확인 실패 상태를 반환합니다.
         * @return 해제 실패 상태
         */
        public CpfLockManager.ReleaseStatus status() { return status; }
    }
}
