package com.cpf.batch.worker;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Reports a collision-resistant worker identity and its current fencing generation. */
@Component
public final class WorkerRegistryReporter {
    private static final Logger log = LoggerFactory.getLogger(WorkerRegistryReporter.class);

    private final SpringBatchWorkerRuntimeState runtime;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;
    private final String systemId;
    private final String serverInstanceId;
    private final String processId;
    private final String restartId = UUID.randomUUID().toString().substring(0, 12);

    public WorkerRegistryReporter(
            SpringBatchWorkerRuntimeState runtime,
            JdbcTemplate jdbc,
            ObjectMapper mapper,
            @Value("${cpf.framework.system-id:${CPF_SYSTEM_ID:BAT}}") String systemId,
            @Value("${cpf.framework.was-id:${CPF_WAS_ID:batWK-local-01}}") String serverInstanceId,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.runtime = runtime;
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.systemId = systemId;
        this.serverInstanceId = serverInstanceId;
        this.processId = processId();
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Scheduled(
            fixedDelayString = "${cpf.batch.worker.registry-heartbeat-ms:5000}",
            initialDelayString = "${cpf.batch.worker.registry-initial-delay-ms:1000}")
    public void heartbeat() {
        String host = resolveHost();
        CpfBatchWorkerIdentity identity = new CpfBatchWorkerIdentity(
                systemId,
                serverInstanceId,
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
                serverInstanceId,
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

    private static String processId() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int separator = runtimeName.indexOf('@');
        return separator > 0 ? runtimeName.substring(0, separator) : runtimeName;
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception failure) {
            log.warn("Worker host name resolution failed. cause={}", failure.getClass().getSimpleName());
            return "unresolved";
        }
    }
}
