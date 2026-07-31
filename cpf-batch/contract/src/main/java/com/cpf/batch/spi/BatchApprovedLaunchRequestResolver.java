package com.cpf.batch.spi;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

/** Trigger 또는 운영자 요청을 승인된 Spring Batch 실행 요청으로 해석하는 Control Plane SPI입니다. */
public interface BatchApprovedLaunchRequestResolver {
    BatchApprovedLaunchRequest resolve(TriggerContext context);
    BatchApprovedLaunchRequest resolve(ManualContext context);

    record TriggerContext(
            String scheduleId, String jobId, long definitionVersion, String definitionChecksum,
            LocalDate businessDate, OffsetDateTime scheduledAt, long fencingToken, String idempotencyKey) { }

    record ManualContext(
            String approvalId, String operatorId, String reason, String idempotencyKey,
            long fencingToken, Map<String,Object> parameters) {
        public ManualContext { parameters = parameters == null ? Map.of() : Map.copyOf(parameters); }
    }
}
