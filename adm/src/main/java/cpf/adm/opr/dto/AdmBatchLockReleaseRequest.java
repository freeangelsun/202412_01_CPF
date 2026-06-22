package cpf.adm.opr.dto;

/**
 * 배치 lock 강제 해제 요청입니다.
 *
 * @param lockKey 해제할 lock key
 * @param requestUser 요청 운영자 ID
 * @param reason 감사 로그와 운영 로그에 남길 해제 사유
 */
public record AdmBatchLockReleaseRequest(
        String lockKey,
        String requestUser,
        String reason) {
}
