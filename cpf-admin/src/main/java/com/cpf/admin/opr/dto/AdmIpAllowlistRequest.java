package cpf.adm.opr.dto;

/**
 * ADM IP 허용 목록 변경 요청입니다.
 *
 * @param ipPattern   허용 IP 또는 CIDR 패턴
 * @param description 설명
 * @param useYn       사용 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmIpAllowlistRequest(
        String ipPattern,
        String description,
        String useYn,
        String requestUser,
        String reason) {
}
