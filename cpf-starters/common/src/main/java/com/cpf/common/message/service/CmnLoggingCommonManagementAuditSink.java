package com.cpf.common.message.service;

import com.cpf.common.management.CpfCommonManagementAuditSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/** 별도 Audit adapter가 없을 때 민감정보를 제거한 구조화 감사로그를 남기는 안전한 기본 구현입니다. */
public final class CmnLoggingCommonManagementAuditSink implements CpfCommonManagementAuditSink {
    private static final Logger log = LoggerFactory.getLogger(CmnLoggingCommonManagementAuditSink.class);

    @Override
    public void record(String action, String resourceType, String resourceKey, String actor, String reason,
                       Map<String, Object> safeAttributes) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (safeAttributes != null) safeAttributes.forEach((k, v) -> attributes.put(safe(k), safe(v)));
        log.info("CPF_COMMON_AUDIT action={} resourceType={} resourceKey={} actor={} reason={} attributes={}",
                safe(action), safe(resourceType), safe(resourceKey), safe(actor), safe(reason), attributes);
    }

    private String safe(Object value) {
        if (value == null) return "-";
        String s = String.valueOf(value).replace('\r', ' ').replace('\n', ' ');
        if (s.length() > 256) s = s.substring(0, 256);
        return s.replaceAll("(?i)(password|secret|token|authorization|cookie|credential)[^, }]*", "***");
    }
}
