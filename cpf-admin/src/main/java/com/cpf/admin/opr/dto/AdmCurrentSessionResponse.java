package com.cpf.admin.opr.dto;

import java.util.List;

/** 현재 ADM 운영자 세션과 허용 메뉴입니다. */
public record AdmCurrentSessionResponse(
        String operatorId,
        List<String> roleIds,
        boolean passwordChangeRequired,
        List<AdmMenu> menus,
        List<String> buttonIds) {
    public AdmCurrentSessionResponse {
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
        menus = menus == null ? List.of() : List.copyOf(menus);
        buttonIds = buttonIds == null ? List.of() : List.copyOf(buttonIds);
    }
}
