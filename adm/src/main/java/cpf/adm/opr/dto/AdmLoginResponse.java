package cpf.adm.opr.dto;

import java.util.List;

/**
 * ADM 濡쒓렇???묐떟?낅땲??
 *
 * @param accessToken      ADM API ?몄텧???ъ슜??Bearer ?좏겙
 * @param tokenType        ?좏겙 ?좏삎
 * @param expiresInSeconds ?좏겙 留뚮즺源뚯? ?⑥? 珥? * @param operator         濡쒓렇???댁쁺???뺣낫
 * @param menus            ?댁쁺??沅뚰븳 湲곗??쇰줈 ?쒖떆??硫붾돱 ⑸줉
 */
public record AdmLoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AdmOperator operator,
        List<AdmMenu> menus) {
}

