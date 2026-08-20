package com.cpf.batch.worker;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Reports a collision-resistant worker identity and its current fencing generation. */
@Component
public final class WorkerRegistryReporter {
    private final SpringBatchWorkerRuntimeState runtime;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;
    private final String systemId;
    private final String instanceId;
    private final String processId;
    private final String restartId = UUID.randomUUID().toString().substring(0, 12);

    public WorkerRegistryReporter(
            SpringBatchWorkerRuntimeState runtime,
            JdbcTemplate jdbc,
            ObjectMapper mapper,
            @Value("${cpf.framework.system-id:${CPF_SYSTEM_ID:BAT}}") String systemId,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.runtime = runtime;
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.systemId = systemId;
        CpfInstanceIdentity.Identity runtimeIdentity = CpfInstanceIdentity.current();
        this.instanceId = runtimeIdentity.instanceId();
        this.processId = runtimeIdentity.processId();
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Scheduled(
            fixedDelayString = "${cpf.batch.worker.registry-heartbeat-ms:5000}",
            initialDelayString = "${cpf.batch.worker.registry-initial-delay-ms:1000}")
    public void heartbeat() {
        String host = CpfInstanceIdentity.current().hostName();
        CpfBatchWorkerIdentity identity = new CpfBatchWorkerIdentity(
                systemId,
                instanceId,
                processId,
                restartId,
                runtime.leaseEpoch(),
                runtime.fencingToken());
        String capabilities = serializeCapabilities(identity);
        String state = runtime.draining()
                ? "DRAINING"
                : runtime.currentJobExecutionId() == null ? "IDLE" : "RUNNING";
        jdbc.update(
                sql.required("worker-registry-upsert"),
                identity.canonicalId(),
                instanceId,
                host,
                processId,
                runtime.workerVersion(),
                capabilities,
                runtime.configuredMaxConcurrency(),
                runtime.draining() ? "DRAINING" : "RUNNING",
                state,
                runtime.currentJobExecutionId());
    }

    private String serializeCapabilities(CpfBatchWorkerIdentity identity) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("capabilities", runtime.capabilities());
        value.put("identity", Map.of(
                "systemId", identity.systemId(),
                "instanceId", identity.instanceId(),
                "processId", identity.processId(),
                "restartId", identity.restartId(),
                "leaseEpoch", identity.leaseEpoch(),
                "fencingToken", identity.fencingToken()));
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException(
                    "Worker capabilities cannot be serialized; registry heartbeat is aborted",
                    failure);
        }
    }

}
