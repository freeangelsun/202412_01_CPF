package cpf.adm.opr.dto;

/**
 * 배치 ghost 후보에 대한 운영 조치 요청입니다.
 *
 * @param actionType 조치 유형. FAIL, ABANDON, RELEASE_LOCK 중 하나를 권장합니다.
 * @param requestUser 요청 운영자 ID
 * @param reason 감사 로그와 ghost 이벤트에 남길 운영 사유
 */
public record AdmBatchGhostActionRequest(
        String actionType,
        String requestUser,
        String reason) {
}
