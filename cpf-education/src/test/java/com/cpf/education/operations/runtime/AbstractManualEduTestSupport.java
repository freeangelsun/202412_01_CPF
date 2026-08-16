package com.cpf.education.operations.runtime;
import com.cpf.education.operations.runtime.application.*;
import com.cpf.education.operations.runtime.model.*;
import com.cpf.education.operations.runtime.persistence.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public abstract class AbstractManualEduTestSupport {
    private static final String DEFAULT_DATABASE_VENDOR = "postgresql";
    protected abstract AbstractEduCapabilityHandler handler();

    protected EduExecutionService service(Path directory) {
        return new EduExecutionService(EduFullEducationTestRegistry.create(), new FileEduOperationRepository(directory),
                TestEduBusinessConsumers.registry(), Clock.systemUTC(), "test-instance");
    }

    protected EduFailurePoint failure() {
        return handler().definition().supportedFailures().stream()
                .filter(point -> point != EduFailurePoint.NONE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(handler().definition().requirementId() + " has no failure point"));
    }

    protected EduExecutionCommand command(EduFailurePoint failure) {
        EduCapabilityDefinition definition = handler().definition();
        Map<String,Object> payload = validPayload();
        return new EduExecutionCommand("business-" + definition.requirementId(), UUID.randomUUID().toString(),
                0, "tester", Set.of(definition.requiredRole()), "TENANT-A", "automated verification",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload, failure, true, true);
    }

    protected EduExecutionCommand commandWithPayload(Map<String,Object> payload) {
        EduCapabilityDefinition definition = handler().definition();
        return new EduExecutionCommand("business-" + definition.requirementId(), UUID.randomUUID().toString(),
                0, "tester", Set.of(definition.requiredRole()), "TENANT-A", "negative verification",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload,
                EduFailurePoint.NONE, true, true);
    }

    protected Map<String,Object> validPayload() {
        Map<String,Object> payload = new LinkedHashMap<>();
        for (String field : handler().definition().requiredFields()) payload.put(field, sample(field));
        return Map.copyOf(payload);
    }

    protected Object sample(String field) {
        return switch (field) {
            case "amount" -> "1000.00";
            case "pageSize", "chunkSize", "batchSize", "gridSize", "queueSize", "contentLength",
                    "requestedUnits", "memberCount", "partitionCount", "timeout", "duration", "threshold" -> 4;
            case "port" -> 18080;
            case "trafficWeight", "weight", "maxUnavailable" -> 10;
            case "checksum" -> "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            case "dbVendor", "databaseVendor", "vendor" -> DEFAULT_DATABASE_VENDOR;
            case "route", "healthPath" -> "/edu/health";
            case "artifactPath", "installDir", "fileName" -> "build/edu/input.dat";
            case "endpoint", "endpointAlias", "callbackUrl" -> "https://partner.example/edu";
            case "validFrom", "effectiveFrom" -> "2026-01-01";
            case "validTo", "effectiveTo" -> "2026-12-31";
            case "fromVersion", "blueVersion" -> "1.0.0";
            case "toVersion", "greenVersion" -> "1.1.0";
            case "sort" -> "updatedAt,desc";
            case "businessDate", "effectiveDate", "targetDate" -> "2026-08-01";
            default -> field + "-value";
        };
    }
}
