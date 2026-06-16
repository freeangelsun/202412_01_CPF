package cpf.adm.opr.dto;

import java.util.List;

/**
 * 愿由ъ옄 ?댁쁺??議고쉶 ?묐떟?낅땲??
 *
 * @param operatorId             ?댁쁺??ID
 * @param operatorName           ?댁쁺?먮챸
 * @param roleIds                遺????븷 紐⑸줉
 * @param locked                 怨꾩젙 ?좉툑 ?щ?
 * @param passwordExpired        鍮꾨?踰덊샇 留뚮즺 ?щ?
 * @param passwordChangeRequired 理쒖큹/媛뺤젣 蹂寃??щ?
 * @param createdAt              ?깅줉 ?쒓컖
 * @param updatedAt              ?섏젙 ?쒓컖
 */
public record AdmOperator(
        String operatorId,
        String operatorName,
        List<String> roleIds,
        boolean locked,
        boolean passwordExpired,
        boolean passwordChangeRequired,
        String createdAt,
        String updatedAt) {
}

