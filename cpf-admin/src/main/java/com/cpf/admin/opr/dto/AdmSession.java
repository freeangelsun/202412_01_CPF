package cpf.adm.opr.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ADM 인증 세션 상태입니다.
 *
 * @param token      API Bearer 토큰
 * @param operatorId 운영자 ID
 * @param roleIds    운영자 역할 ID 목록
 * @param passwordChangeRequired 비밀번호 변경 전용 세션 여부
 * @param issuedAt   발급일시
 * @param expiresAt  만료일시
 */
public record AdmSession(
        String token,
        String operatorId,
        List<String> roleIds,
        boolean passwordChangeRequired,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt) {
}
