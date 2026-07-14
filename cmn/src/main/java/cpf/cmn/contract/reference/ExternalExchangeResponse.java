package cpf.cmn.contract.reference;

/**
 * EXS 외부 송신 선저장 결과의 공개 응답 계약입니다.
 */
public record ExternalExchangeResponse(
        String transactionGlobalId,
        String transactionSegmentId,
        String externalTransactionId,
        String direction,
        String preSavedYn,
        String status) {
}
