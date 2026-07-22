package cpf.pfw.common.idempotency;

/**
 * 멱등 실행 결과와 저장 응답 재사용 여부를 함께 전달합니다.
 */
public record CpfIdempotencyExecutionResult(
        String status,
        String response,
        boolean replayed,
        String transactionGlobalId,
        String segmentId) {
}
