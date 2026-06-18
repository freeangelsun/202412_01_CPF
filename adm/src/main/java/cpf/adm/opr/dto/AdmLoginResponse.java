package cpf.adm.opr.dto;

import java.util.List;

/**
 * ADM 운영자 로그인 응답입니다.
 *
 * @param accessToken      ADM API 호출에 사용할 접근 토큰
 * @param tokenType        토큰 유형
 * @param expiresInSeconds 토큰 만료까지 남은 초
 * @param operator         로그인한 운영자 정보
 * @param menus            운영자 권한 기준으로 노출할 메뉴 목록
 */
public record AdmLoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AdmOperator operator,
        List<AdmMenu> menus) {
}
