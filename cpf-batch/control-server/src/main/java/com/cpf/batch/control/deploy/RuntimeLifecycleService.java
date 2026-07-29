package com.cpf.batch.control.deploy;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class RuntimeLifecycleService {
    private final JdbcTemplate jdbc;
    private final RestClient.Builder builder;
    private final CpfVendorSqlCatalog sql;

    public RuntimeLifecycleService(
            JdbcTemplate jdbc,
            RestClient.Builder builder,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.builder = builder;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    public AgentCommandResult operate(
            String instanceId,
            String operation,
            String requestedBy,
            String reason) {
        require(instanceId, "instanceId");
        require(requestedBy, "requestedBy");
        require(reason, "reason");
        String normalized = operation.toLowerCase(Locale.ROOT);
        if (!Set.of("start", "stop", "restart", "drain", "resume", "rollback", "status")
                .contains(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported runtime operation: " + operation);
        }
        Map<String, Object> row = jdbc.queryForMap(
                sql.required("deploy-runtime-route"),
                instanceId);
        String agent = Objects.toString(row.get("agent_base_url"), "");
        String service = Objects.toString(row.get("service_id"), "");
        if (agent.isBlank() || service.isBlank()) {
            throw new IllegalStateException(
                    "Deployment inventory is incomplete for " + instanceId);
        }
        try {
            AgentCommandResult result = builder
                    .baseUrl(agent)
                    .build()
                    .post()
                    .uri("/api/v1/agent/services/{service}/{op}", service, normalized)
                    .retrieve()
                    .body(AgentCommandResult.class);
            return result == null ? unknown(service, normalized, "NO_RESULT") : result;
        } catch (RuntimeException exception) {
            return unknown(service, normalized, "TRANSPORT_UNKNOWN");
        }
    }

    private static AgentCommandResult unknown(String service, String operation, String code) {
        Instant now = Instant.now();
        return new AgentCommandResult(
                UUID.randomUUID().toString(),
                service,
                operation,
                CommandState.UNKNOWN_RESULT,
                code,
                "Agent result is unknown; reconcile before retry",
                null,
                now,
                now);
    }

    private static void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
