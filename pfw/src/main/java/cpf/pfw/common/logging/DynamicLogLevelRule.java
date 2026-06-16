package cpf.pfw.common.logging;

import java.time.LocalDateTime;

/**
 * ?댁쁺 以??뱀젙 嫄곕옒??吏꾨떒 濡쒓렇 ?덈꺼???꾩떆濡?議곗젙?섍린 ?꾪븳 洹쒖튃?낅땲??
 *
 * @param ruleId                洹쒖튃 ?앸퀎?? * @param transactionId         湲濡쒕쾶 嫄곕옒ID. ?뱀젙 ??嫄대쭔 異붿쟻?????ъ슜?⑸땲??
 * @param businessTransactionId 而⑦듃濡ㅻ윭 ?낅Т 嫄곕옒ID. ?뱀젙 API留?異붿쟻?????ъ슜?⑸땲??
 * @param moduleId              二쇱젣?곸뿭 肄붾뱶. 鍮꾩뼱 ?덉쑝硫??꾩껜 二쇱젣?곸뿭???곸슜?⑸땲??
 * @param logLevel              ?곸슜??濡쒓렇 ?덈꺼
 * @param reason                ?댁쁺?먭? 洹쒖튃???깅줉???ъ쑀
 * @param createdBy             ?깅줉?? * @param createdAt             ?깅줉 ?쒓컖
 * @param expiresAt             留뚮즺 ?쒓컖
 */
public record DynamicLogLevelRule(
        String ruleId,
        String transactionId,
        String businessTransactionId,
        String moduleId,
        FpsLogLevel logLevel,
        String reason,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

    /**
     * ?꾩옱 ?쒓컖 湲곗??쇰줈 洹쒖튃??留뚮즺?섏뿀?붿? ?뺤씤?⑸땲??
     *
     * @param now ?꾩옱 ?쒓컖
     * @return 留뚮즺?섏뿀?쇰㈃ true
     */
    public boolean expired(LocalDateTime now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }
}

