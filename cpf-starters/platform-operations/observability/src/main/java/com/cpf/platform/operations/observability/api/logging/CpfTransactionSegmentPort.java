package com.cpf.platform.operations.observability.api.logging;

/**
 * Module 경계를 넘는 거래 구간 추적용 공개 Port입니다.
 * Consumer는 Observability 내부 저장 구조를 참조하지 않고 시작/종료 의미만 사용합니다.
 */
public interface CpfTransactionSegmentPort {
    enum Role { MAIN, SUB, SHARED, EXTERNAL, BATCH, CENTER_CUT_PARENT, CENTER_CUT_CHILD, OPERATOR_ACTION }
    enum Direction { INBOUND, OUTBOUND, INTERNAL, RESPONSE }

    SegmentScope start(Role role, Direction direction, String moduleCode, String sourceModuleCode,
            String targetModuleCode, String apiPath, String transactionName);

    /** 한 번만 성공/실패로 종료되는 거래 구간 Scope입니다. */
    interface SegmentScope extends AutoCloseable {
        String transactionSegmentId();
        String transactionId();
        /** transport/runtime 선택 결과를 저장 구현을 노출하지 않고 Segment에 보강합니다. */
        void update(SegmentAttributes attributes);
        void success();
        void fail(String failureCode, String failureMessage);
        @Override default void close() { success(); }
    }

    /** Remote/Domain Call attempt에서 운영 Timeline에 필요한 bounded metadata입니다. */
    record SegmentAttributes(String selectedInstanceId, Integer attemptNo, Boolean retry, Boolean failover,
            String circuitState, Integer downstreamStatus, String resultState, String unknownResultId) { }
}
