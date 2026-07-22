package cpf.adm.opr.dto;

/**
 * DLQ 재처리와 결과 미확정 수동 확정에 사용하는 ADM 운영 요청입니다.
 */
public record AdmReliabilityActionRequest(
        String targetStatus,
        String reason,
        String requestUser) {
}
