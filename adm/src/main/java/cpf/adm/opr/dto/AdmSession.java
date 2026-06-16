package cpf.adm.opr.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ADM ?몃찓紐⑤━ ?몄뀡 ?뺣낫?낅땲??
 *
 * @param token      API Bearer ?좏겙
 * @param operatorId ?댁쁺??ID
 * @param roleIds    ?댁쁺????븷 紐⑸줉
 * @param issuedAt   諛쒓툒 ?쒓컖
 * @param expiresAt  留뚮즺 ?쒓컖
 */
public record AdmSession(
        String token,
        String operatorId,
        List<String> roleIds,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt) {
}

