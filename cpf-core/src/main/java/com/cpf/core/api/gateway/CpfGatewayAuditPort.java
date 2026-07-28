package com.cpf.core.api.gateway;

/** Gateway 위험 거래 Audit 저장 Public SPI입니다. durable=true인 Adapter만 auditReasonRequired route에 사용됩니다. */
public interface CpfGatewayAuditPort {
    boolean durable();
    void record(CpfGatewayAuditEvent event);
}
