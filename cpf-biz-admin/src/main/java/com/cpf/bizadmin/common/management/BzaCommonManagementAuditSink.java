package com.cpf.bizadmin.common.management;

import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.common.management.CpfCommonManagementAuditSink;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Common owner audit SPI를 BZA tamper-evident audit chain으로 연결하는 Adapter입니다. */
@Component
@Primary
public final class BzaCommonManagementAuditSink implements CpfCommonManagementAuditSink {
    private final BzaBusinessAuditService audit;
    public BzaCommonManagementAuditSink(BzaBusinessAuditService audit) { this.audit = audit; }

    @Override
    public void record(String action, String resourceType, String resourceKey, String actor, String reason,
                       Map<String, Object> safeAttributes) {
        audit.record(actor, "COMMON_" + safe(action), safe(resourceType), safe(resourceKey), reason,
                null, safeAttributes == null ? Map.of() : safeAttributes);
    }
    private String safe(String value) {
        if (value == null || value.isBlank()) return "-";
        String v=value.replace('\r',' ').replace('\n',' ').trim();
        return v.length() <= 160 ? v : v.substring(0,160);
    }
}
