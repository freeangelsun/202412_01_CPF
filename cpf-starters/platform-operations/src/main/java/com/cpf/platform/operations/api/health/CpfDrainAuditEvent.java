package com.cpf.platform.operations.api.health;
import java.time.Instant;
/** 위험 운영조치 Drain/Resume의 감사 이벤트입니다. */
public record CpfDrainAuditEvent(String action, String actor, String reason, String approvalId,
                                 String instanceId, String result, Instant at) {}
