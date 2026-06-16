package cpf.adm.opr.dto;

import java.util.List;

/**
 * ?댁쁺???깅줉 ?붿껌?낅땲??
 *
 * @param operatorId   ?댁쁺??ID
 * @param operatorName ?댁쁺?먮챸
 * @param password     珥덇린 鍮꾨?踰덊샇
 * @param roleIds      ??븷 ID 紐⑸줉
 * @param requestUser  ?깅줉 ?붿껌?? */
public record AdmOperatorCreateRequest(
        String operatorId,
        String operatorName,
        String password,
        List<String> roleIds,
        String requestUser) {
}

