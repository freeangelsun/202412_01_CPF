package com.cpf.common.management;

import java.util.Map;

/** Common owner mutation audit boundary. BZA/Platform adapter owns durable audit persistence. */
public interface CpfCommonManagementAuditSink {
    void record(String action, String resourceType, String resourceKey, String actor, String reason,
                Map<String, Object> safeAttributes);
}
