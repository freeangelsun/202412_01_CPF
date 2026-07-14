package cpf.cmn.contract.reference;

/**
 * ACC가 EXS 호출 결과와 CPF 추적 식별자를 반환하는 공개 계약입니다.
 */
public record AccMemberExternalResponse(
        String transactionGlobalId,
        String transactionSegmentId,
        Integer memberId,
        String externalTransactionId,
        String status,
        String resultCode) {
}
