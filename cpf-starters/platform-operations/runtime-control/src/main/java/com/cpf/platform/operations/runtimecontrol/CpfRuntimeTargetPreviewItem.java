package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;

/** Runtime 변경 후보 instance의 capability·운영상태 판정입니다. */
public record CpfRuntimeTargetPreviewItem(
        String instanceId,
        String serviceId,
        String environment,
        String zone,
        String cell,
        boolean maintenance,
        boolean draining,
        String artifactVersion,
        String artifactCommit,
        String runtimeRole,
        Instant leaseUntil,
        String capability,
        boolean schemaSupported,
        boolean excluded,
        boolean eligible) {
}
