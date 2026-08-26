package com.cpf.data.lock.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/** Topology와 무관하게 lock, lease, owner epoch 및 fencing을 제어하는 CPF 공개 계약입니다. */
public interface CpfLockManager {
    /**
     * 지정한 owner와 요청으로 lease 기반 lock을 획득합니다.
     * @param key lock 자원 키
     * @param ownerId 현재 실행 주체 식별자
     * @param requestId 멱등 재시도 구분용 요청 식별자
     * @param leaseDuration lock lease 유효시간
     * @return 획득, 멱등 재생, 경쟁 또는 UNKNOWN 상태를 포함한 결과
     */
    AcquireResult acquire(String key, String ownerId, String requestId, Duration leaseDuration);
    /**
     * 현재 token의 소유권과 fencing 정보를 유지한 채 lease를 갱신합니다.
     * @param token 이전 획득에서 발급된 완전한 lock token
     * @param leaseDuration 새 lease 유효시간
     * @return 갱신 성공 또는 stale/not-owner/expired/UNKNOWN 상태
     */
    RenewResult renew(LockToken token, Duration leaseDuration);
    /**
     * 현재 token으로 lock을 해제하고 해제 결과를 영속 상태로 확정합니다.
     * @param token 해제할 lock token
     * @param reason 감사 가능한 해제 사유
     * @return 해제 또는 멱등 재생, stale/not-owner/UNKNOWN 상태
     */
    ReleaseResult release(LockToken token, String reason);
    /**
     * 저장소가 현재 fencing token을 최신 writer로 인정하는지 확인합니다.
     * @param key lock 자원 키
     * @param fencingToken 검증할 단조 증가 fencing token
     * @return 최신 유효 token이면 {@code true}, 아니면 {@code false}
     */
    boolean validateFence(String key, long fencingToken);

    /**
     * fencing epoch뿐 아니라 owner, request, version, lease까지 포함한 완전한 token을 검증합니다.
     * 저장소 조회 실패 시 기본 구현은 fail-closed로 {@code false}를 반환합니다.
     * @param token 검증할 lock token
     * @return 현재 ACTIVE snapshot과 모든 낙관적 소유권 정보가 일치하면 {@code true}
     */
    default boolean validateToken(LockToken token) {
        if (token == null || !validateFence(token.key(), token.fencingToken())) {
            return false;
        }
        try {
            return find(token.key())
                    .filter(snapshot -> snapshot.state() == State.ACTIVE)
                    .filter(snapshot -> snapshot.key().equals(token.key()))
                    .filter(snapshot -> token.ownerId().equals(snapshot.ownerId()))
                    .filter(snapshot -> token.requestId().equals(snapshot.requestId()))
                    .filter(snapshot -> snapshot.fencingToken() == token.fencingToken())
                    .filter(snapshot -> snapshot.ownerEpoch() == token.ownerEpoch())
                    .filter(snapshot -> snapshot.version() == token.version())
                    .filter(snapshot -> snapshot.leaseUntil().equals(token.leaseUntil()))
                    .isPresent();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (RuntimeException storageFailure) {
            return false;
        }
    }

    /**
     * 자원 키의 현재 lock snapshot을 조회합니다.
     * @param key lock 자원 키
     * @return 존재하면 현재 snapshot, 없으면 빈 Optional
     */
    Optional<LockSnapshot> find(String key);
    /**
     * 운영 조회용으로 lock snapshot을 제한 개수만큼 조회합니다.
     * @param limit 최대 조회 건수
     * @return 현재 lock snapshot 목록
     */
    List<LockSnapshot> list(int limit);

    /**
     * 빈 결과와 저장소 장애를 구분할 수 있는 상태 포함 단건 조회를 수행합니다.
     * @param key lock 자원 키
     * @return FOUND/NOT_FOUND/INVALID 상태와 snapshot을 포함한 결과
     */
    default FindResult findResult(String key) {
        if (key == null || key.isBlank()) return new FindResult(QueryStatus.INVALID, null, "INVALID_ARGUMENT");
        Optional<LockSnapshot> found = find(key);
        return found.map(snapshot -> new FindResult(QueryStatus.FOUND, snapshot, "FOUND"))
                .orElseGet(() -> new FindResult(QueryStatus.NOT_FOUND, null, "NOT_FOUND"));
    }

    /**
     * 모니터링과 readiness Consumer가 상태를 판별할 수 있는 목록 조회를 수행합니다.
     * @param limit 1 이상 1000 이하 최대 조회 건수
     * @return SUCCESS/INVALID 상태와 snapshot 목록을 포함한 결과
     */
    default ListResult listResult(int limit) {
        if (limit < 1 || limit > 1000) return new ListResult(QueryStatus.INVALID, List.of(), "INVALID_LIMIT");
        return new ListResult(QueryStatus.SUCCESS, list(limit), "SUCCESS");
    }

    /**
     * 승인·SoD·감사 조건을 만족할 때만 운영자가 강제 해제를 수행합니다.
     * @param key 강제 해제할 lock 자원 키
     * @param operatorId 실행 운영자 식별자
     * @param reason 감사 가능한 강제 해제 사유
     * @param approval 실행 명령에 바인딩된 승인 증적
     * @return 강제 해제 결과와 감사 식별자
     */
    ForceReleaseResult forceRelease(
            String key, String operatorId, String reason, ForceReleaseApproval approval);

    /**
     * 자연 만료된 ACTIVE lease를 영속 EXPIRED 상태로 조정합니다.
     * @param limit 한 번에 스캔할 최대 건수
     * @return 스캔·복구·충돌 건수와 상태를 포함한 복구 결과
     */
    default RecoveryResult reconcileExpired(int limit) {
        if (limit < 1 || limit > 1000) return new RecoveryResult(RecoveryStatus.INVALID, 0, 0, 0, "INVALID_LIMIT");
        return new RecoveryResult(RecoveryStatus.UNSUPPORTED, 0, 0, 0, "RECOVERY_NOT_SUPPORTED");
    }

    record LockToken(
            String key,
            String ownerId,
            String requestId,
            long fencingToken,
            long ownerEpoch,
            long version,
            Instant leaseUntil) {
        /** 완전한 lock token의 필수 식별자와 단조 증가 값을 검증합니다. */
        public LockToken {
            if (key == null || key.isBlank() || ownerId == null || ownerId.isBlank()
                    || requestId == null || requestId.isBlank() || fencingToken < 1
                    || ownerEpoch < 1 || version < 1 || leaseUntil == null) {
                throw new IllegalArgumentException("complete lock token is required");
            }
            key = key.trim();
            ownerId = ownerId.trim();
            requestId = requestId.trim();
        }

        /**
         * 낙관적 version 공개 이전 client와의 소스 호환을 위한 생성자입니다.
         * @param key lock 자원 키
         * @param ownerId 소유자 식별자
         * @param requestId 요청 식별자
         * @param fencingToken fencing token
         * @param leaseUntil lease 만료 시각
         */
        public LockToken(
                String key, String ownerId, String requestId, long fencingToken, Instant leaseUntil) {
            this(key, ownerId, requestId, fencingToken, fencingToken, 1L, leaseUntil);
        }
    }

    record LockSnapshot(
            String key,
            String ownerId,
            String requestId,
            long fencingToken,
            long ownerEpoch,
            long version,
            Instant acquiredAt,
            Instant leaseUntil,
            State state) {
        /** 운영 조회 snapshot의 필수 식별자·fencing·시간 상태를 검증합니다. */
        public LockSnapshot {
            if (key == null || key.isBlank() || fencingToken < 1 || ownerEpoch < 1 || version < 1
                    || acquiredAt == null || leaseUntil == null || state == null) {
                throw new IllegalArgumentException("complete lock snapshot is required");
            }
        }

        /**
         * 초기 fencing-only snapshot 계약과의 소스 호환을 위한 생성자입니다.
         * @param key lock 자원 키
         * @param ownerId 소유자 식별자
         * @param requestId 요청 식별자
         * @param fencingToken fencing token
         * @param acquiredAt 획득 시각
         * @param leaseUntil lease 만료 시각
         * @param state snapshot 상태
         */
        public LockSnapshot(
                String key,
                String ownerId,
                String requestId,
                long fencingToken,
                Instant acquiredAt,
                Instant leaseUntil,
                State state) {
            this(key, ownerId, requestId, fencingToken, fencingToken, 1L,
                    acquiredAt, leaseUntil, state);
        }
    }


    /** QueryStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    enum QueryStatus { SUCCESS, FOUND, NOT_FOUND, UNKNOWN, INVALID }
    record FindResult(QueryStatus status, LockSnapshot snapshot, String reason) {}
    record ListResult(QueryStatus status, List<LockSnapshot> snapshots, String reason) {
        public ListResult { snapshots = snapshots == null ? List.of() : List.copyOf(snapshots); }
    }

    enum State { ACTIVE, RELEASED, EXPIRED, FORCE_RELEASED }
    enum AcquireStatus { ACQUIRED, IDEMPOTENT_REPLAY, BUSY, RESOURCE_EXHAUSTED, UNKNOWN, INVALID }
    record AcquireResult(AcquireStatus status, LockToken token, LockSnapshot current, String reason) {}
    /** RenewStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    enum RenewStatus { RENEWED, STALE_TOKEN, NOT_OWNER, NOT_FOUND, EXPIRED, UNKNOWN, INVALID }
    record RenewResult(RenewStatus status, LockToken token, String reason) {}
    enum ReleaseStatus { RELEASED, IDEMPOTENT_REPLAY, STALE_TOKEN, NOT_OWNER, NOT_FOUND, EXPIRED, UNKNOWN, INVALID }
    record ReleaseResult(ReleaseStatus status, LockSnapshot snapshot, String reason) {}

    record ForceReleaseCommand(
            String key,
            String requesterId,
            String reason,
            long expectedFencingToken,
            long expectedVersion) {
        /** 강제 해제 명령의 키·요청자·사유·기대 fencing/version을 검증합니다. */
        public ForceReleaseCommand {
            if (key == null || key.isBlank() || requesterId == null || requesterId.isBlank()
                    || reason == null || reason.isBlank() || expectedFencingToken < 1
                    || expectedVersion < 1) {
                throw new IllegalArgumentException("complete force-release command is required");
            }
            key = key.trim();
            requesterId = requesterId.trim();
            reason = reason.trim();
        }

        /**
         * 이전 명령 형식과의 호환을 제공하되 version 1에만 제한하여 최신 상태 승인을 차단합니다.
         * @param key lock 자원 키
         * @param requesterId 강제 해제 요청자
         * @param reason 강제 해제 사유
         * @param expectedFencingToken 기대 fencing token
         */
        public ForceReleaseCommand(
                String key, String requesterId, String reason, long expectedFencingToken) {
            this(key, requesterId, reason, expectedFencingToken, 1L);
        }

        /**
         * 승인 scope에 바인딩할 불변 명령 SHA-256 해시를 계산합니다.
         * @return 소문자 16진수 SHA-256 명령 해시
         */
        public String immutableHash() {
            String canonical = key + "\n" + requesterId + "\n" + reason + "\n"
                    + expectedFencingToken + "\n" + expectedVersion;
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(canonical.getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException impossible) {
                throw new IllegalStateException("SHA-256 is unavailable", impossible);
            }
        }

        /**
         * 민감한 강제 해제 사유를 마스킹한 감사용 문자열을 반환합니다.
         * @return reason 원문을 포함하지 않는 명령 요약
         */
        @Override
        public String toString() {
            return "ForceReleaseCommand[key=" + key
                    + ", requesterId=" + requesterId
                    + ", reason=[REDACTED]"
                    + ", expectedFencingToken=" + expectedFencingToken
                    + ", expectedVersion=" + expectedVersion + "]";
        }
    }

    /** ForceReleaseApproval 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ForceReleaseApproval(
            String approvalId,
            String approverId,
            Instant approvedAt,
            Instant expiresAt,
            String commandHash) {
        /** 승인 식별자·승인자·유효시간·선택적 명령 해시의 형식을 검증합니다. */
        public ForceReleaseApproval {
            if (approvalId == null || approvalId.isBlank()
                    || approverId == null || approverId.isBlank()
                    || approvedAt == null || expiresAt == null
                    || !expiresAt.isAfter(approvedAt)) {
                throw new IllegalArgumentException("complete, bounded approval is required");
            }
            approvalId = approvalId.trim();
            approverId = approverId.trim();
            commandHash = commandHash == null ? null : commandHash.trim().toLowerCase(java.util.Locale.ROOT);
            if (commandHash != null && !commandHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("commandHash must be a lowercase SHA-256 value");
            }
        }

        /**
         * 이전 unscoped 승인 형식과의 호환 생성자이며 실행 단계에서 fail-closed 처리됩니다.
         * @param approvalId 승인 식별자
         * @param approverId 승인자 식별자
         * @param approvedAt 승인 시각
         * @param expiresAt 승인 만료 시각
         */
        public ForceReleaseApproval(
                String approvalId, String approverId, Instant approvedAt, Instant expiresAt) {
            this(approvalId, approverId, approvedAt, expiresAt, null);
        }

        /**
         * 특정 강제 해제 명령 해시에 바인딩된 승인을 생성합니다.
         * @param approvalId 승인 식별자
         * @param approverId 승인자 식별자
         * @param command 승인할 불변 강제 해제 명령
         * @param approvedAt 승인 시각
         * @param expiresAt 승인 만료 시각
         * @return 명령 해시가 바인딩된 승인 객체
         */
        public static ForceReleaseApproval approve(
                String approvalId,
                String approverId,
                ForceReleaseCommand command,
                Instant approvedAt,
                Instant expiresAt) {
            return new ForceReleaseApproval(approvalId, approverId, approvedAt, expiresAt,
                    command.immutableHash());
        }
    }


    /** RecoveryStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    enum RecoveryStatus { SUCCESS, PARTIAL, UNKNOWN, INVALID, UNSUPPORTED }
    record RecoveryResult(RecoveryStatus status, int scanned, int recovered, int conflicts, String reason) {
        /** 복구 카운터가 음수가 되지 않도록 검증합니다. */
        public RecoveryResult {
            if (scanned < 0 || recovered < 0 || conflicts < 0) {
                throw new IllegalArgumentException("recovery counters must be non-negative");
            }
        }
    }

    enum ForceReleaseStatus {
        RELEASED, IDEMPOTENT_REPLAY, NOT_FOUND, APPROVAL_REQUIRED,
        SEPARATION_OF_DUTIES, APPROVAL_EXPIRED, APPROVAL_WINDOW_EXCEEDED, APPROVAL_SCOPE_MISMATCH,
        AUDIT_UNAVAILABLE, UNKNOWN, INVALID
    }
    /** ForceReleaseResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ForceReleaseResult(ForceReleaseStatus status, LockSnapshot snapshot, String auditId, String reason) {}
}
