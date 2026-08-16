package com.cpf.integration.fixedlength.api;

import java.util.List;
import java.util.Map;

/** 운영 로그에 노출 가능한 마스킹 완료 고정길이 전문 View입니다. */
public record CpfFixedLengthLogView(
        String layoutId,
        String version,
        int byteLength,
        Map<String,String> fields,
        Map<String,List<Map<String,String>>> groups) {
    public CpfFixedLengthLogView {
        fields = fields == null ? Map.of() : Map.copyOf(fields);
        groups = groups == null ? Map.of() : Map.copyOf(groups);
    }
}
