package com.cpf.batch.control.deploy;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** 승인된 Runtime 운영 명령을 안정 Command ID로 Host Agent에 전달하고 결과불명 거래를 재조회합니다. */
@Service
public final class RuntimeLifecycleService {
    private static final String COMMAND_ID = "X-CPF-Command-ID";
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final Set<String> OPERATIONS = Set.of("start", "stop", "restart", "drain", "resume", "rollback", "status");

    private final JdbcTemplate jdbc;
    private final RestClient.Builder builder;
    private final CpfVendorSqlCatalog sql;

    public RuntimeLifecycleService(
            JdbcTemplate jdbc,
            RestClient.Builder builder,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.builder = Objects.requireNonNull(builder, "builder");
        this.sql = Objects.requireNonNull(sqlCatalogProvider, "sqlCatalogProvider").forModule("bat");
    }

    public AgentCommandResult operate(
            String instanceId,
            String operation,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
        String safeInstanceId = require(instanceId, "instanceId");
        String safeRequestedBy = require(requestedBy, "requestedBy");
        String safeApprovedBy = require(approvedBy, "approvedBy");
        String safeApprovalRequestId = require(approvalRequestId, "approvalRequestId");
        if (safeRequestedBy.equals(safeApprovedBy)) throw new SecurityException("REQUESTER_APPROVER_SEPARATION_REQUIRED");
        String safeReason = require(reason, "reason");
        String normalized = require(operation, "operation").toLowerCase(Locale.ROOT);
        if (!OPERATIONS.contains(normalized)) throw new IllegalArgumentException("Unsupported runtime operation: " + operation);

        Map<String, Object> row = jdbc.queryForMap(sql.required("deploy-runtime-route"), safeInstanceId);
        URI agentUri = validatedAgentUri(Objects.toString(row.get("agent_base_url"), ""));
        String service = require(Objects.toString(row.get("service_id"), ""), "serviceId");
        String commandId = commandId(safeInstanceId, service, normalized, safeRequestedBy,
                safeApprovedBy, safeApprovalRequestId, safeReason);
        RestClient client = client(agentUri);
        try {
            AgentCommandResult result = client.post()
                    .uri("/api/v1/agent/services/{service}/{op}", service, normalized)
                    .header(COMMAND_ID, commandId)
                    .header(IDEMPOTENCY_KEY, commandId)
                    .retrieve()
                    .body(AgentCommandResult.class);
            return checked(commandId, service, normalized, result);
        } catch (RuntimeException transportFailure) {
            return reconcile(client, commandId, service, normalized, transportFailure);
        }
    }

    private AgentCommandResult reconcile(
            RestClient client, String commandId, String service, String operation, RuntimeException transportFailure) {
        try {
            AgentCommandResult result = client.get().uri("/api/v1/agent/commands/{commandId}", commandId)
                    .retrieve().body(AgentCommandResult.class);
            return checked(commandId, service, operation, result);
        } catch (RuntimeException reconciliationFailure) {
            return unknown(commandId, service, operation,
                    "TRANSPORT_AND_RECONCILIATION_UNKNOWN:" + transportFailure.getClass().getSimpleName()
                            + ":" + reconciliationFailure.getClass().getSimpleName());
        }
    }

    private RestClient client(URI agentUri) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return builder.clone().requestFactory(requestFactory).baseUrl(agentUri.toString()).build();
    }

    private static AgentCommandResult checked(
            String commandId, String service, String operation, AgentCommandResult result) {
        if (result == null) return unknown(commandId, service, operation, "NO_RESULT");
        if (!commandId.equals(result.commandId())) {
            Instant now = Instant.now();
            return new AgentCommandResult(commandId, service, operation, CommandState.FAILED,
                    "AGENT_COMMAND_ID_MISMATCH", "Agent returned a result for another command", null, now, now);
        }
        return result;
    }

    static URI validatedAgentUri(String value) {
        String candidate = require(value, "agentBaseUrl");
        URI uri = URI.create(candidate).normalize();
        if (!uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null || containsControl(candidate)) {
            throw new SecurityException("AGENT_URL_INVALID");
        }
        if (uri.getPath() != null && uri.getPath().contains("..")) throw new SecurityException("AGENT_URL_PATH_INVALID");
        if (!"https".equalsIgnoreCase(uri.getScheme()) && !("http".equalsIgnoreCase(uri.getScheme()) && isLoopback(uri.getHost()))) {
            throw new SecurityException("AGENT_URL_REQUIRES_HTTPS");
        }
        return uri;
    }

    static String commandId(
            String instanceId,
            String service,
            String operation,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
        String requester = require(requestedBy, "requestedBy");
        String approver = require(approvedBy, "approvedBy");
        if (requester.equals(approver)) throw new SecurityException("REQUESTER_APPROVER_SEPARATION_REQUIRED");
        String material = require(instanceId, "instanceId") + "|" + require(service, "service") + "|"
                + require(operation, "operation").toLowerCase(Locale.ROOT) + "|" + requester + "|" + approver
                + "|" + require(approvalRequestId, "approvalRequestId") + "|" + require(reason, "reason");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            return "batctl-" + HexFormat.of().formatHex(digest);
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static AgentCommandResult unknown(String commandId, String service, String operation, String code) {
        Instant now = Instant.now();
        return new AgentCommandResult(commandId, service, operation, CommandState.UNKNOWN_RESULT, code,
                "Agent result is unknown; reconcile before retry", null, now, now);
    }

    private static String require(String value, String fieldName) {
        if (value == null || value.isBlank() || containsControl(value)) throw new IllegalArgumentException(fieldName + " is required");
        String safe = SensitiveTextSanitizer.sanitize(value.trim());
        if (safe.length() > 512) throw new IllegalArgumentException(fieldName + " is too long");
        return safe;
    }

    private static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++) if (Character.isISOControl(value.charAt(i))) return true;
        return false;
    }

    private static boolean isLoopback(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host);
    }
}
