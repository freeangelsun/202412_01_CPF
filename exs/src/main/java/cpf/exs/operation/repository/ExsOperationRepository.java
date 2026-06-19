package cpf.exs.operation.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * EXS 운영 데이터를 exsDB에 영속화하는 저장소입니다.
 *
 * <p>대외 token, token 이벤트, 재처리, 통제 정책, 송수신 선저장 로그를 DB 기준으로 관리합니다.
 * datasource가 비활성화된 경우에는 임시 메모리 저장소로 대체하지 않고 명확히 실패시킵니다.</p>
 */
@Repository
public class ExsOperationRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<PlatformTransactionManager> transactionManagerProvider;

    public ExsOperationRepository(
            @Qualifier("exsJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            @Qualifier("exsTransactionManager") ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.transactionManagerProvider = transactionManagerProvider;
    }

    /**
     * 대외 token 상태를 upsert합니다.
     */
    public void upsertToken(TokenWrite row) {
        jdbc().update("""
                INSERT INTO exs_token_store (
                    auth_profile_code,
                    token_key,
                    token_hash,
                    masked_token,
                    token_status,
                    issued_at,
                    expire_at,
                    transaction_global_id,
                    server_instance_id,
                    created_by,
                    updated_by
                )
                VALUES (
                    :authProfileCode,
                    :tokenKey,
                    :tokenHash,
                    :maskedToken,
                    :tokenStatus,
                    :issuedAt,
                    :expireAt,
                    :transactionGlobalId,
                    :serverInstanceId,
                    :requestUser,
                    :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    token_hash = VALUES(token_hash),
                    masked_token = VALUES(masked_token),
                    token_status = VALUES(token_status),
                    issued_at = VALUES(issued_at),
                    expire_at = VALUES(expire_at),
                    transaction_global_id = VALUES(transaction_global_id),
                    server_instance_id = VALUES(server_instance_id),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """, tokenParams(row));
    }

    /**
     * 대외 token 이벤트 이력을 저장합니다.
     */
    public void insertTokenEvent(TokenEventWrite row) {
        jdbc().update("""
                INSERT INTO exs_token_event_history (
                    auth_profile_code,
                    token_key,
                    event_type,
                    reason,
                    transaction_global_id,
                    server_instance_id,
                    created_by,
                    updated_by
                )
                VALUES (
                    :authProfileCode,
                    :tokenKey,
                    :eventType,
                    :reason,
                    :transactionGlobalId,
                    :serverInstanceId,
                    :requestUser,
                    :requestUser
                )
                """, new MapSqlParameterSource()
                .addValue("authProfileCode", row.authProfileCode())
                .addValue("tokenKey", row.tokenKey())
                .addValue("eventType", row.eventType())
                .addValue("reason", row.reason())
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("serverInstanceId", row.serverInstanceId())
                .addValue("requestUser", row.requestUser()));
    }

    /**
     * 대외 token 상태 목록을 조회합니다.
     */
    public List<Map<String, Object>> findTokens() {
        return jdbc().queryForList("""
                SELECT auth_profile_code AS authProfileCode,
                       token_key AS tokenKey,
                       CONCAT(SUBSTRING(token_hash, 1, 12), '...') AS tokenHashPreview,
                       masked_token AS maskedToken,
                       token_status AS tokenStatus,
                       issued_at AS issuedAt,
                       expire_at AS expireAt,
                       transaction_global_id AS transactionGlobalId,
                       server_instance_id AS serverInstanceId,
                       updated_by AS requestUser,
                       updated_at AS updatedAt
                  FROM exs_token_store
                 ORDER BY auth_profile_code, token_key
                """, Map.of());
    }

    /**
     * 대외 token 이벤트 이력을 조회합니다.
     */
    public List<Map<String, Object>> findTokenEvents(int limit) {
        return jdbc().queryForList("""
                SELECT token_event_id AS tokenEventId,
                       auth_profile_code AS authProfileCode,
                       token_key AS tokenKey,
                       event_type AS eventType,
                       reason,
                       transaction_global_id AS transactionGlobalId,
                       server_instance_id AS serverInstanceId,
                       created_by AS requestUser,
                       created_at AS eventAt
                  FROM exs_token_event_history
                 ORDER BY token_event_id DESC
                 LIMIT :limit
                """, new MapSqlParameterSource("limit", limit));
    }

    /**
     * 대외 재처리 요청을 저장합니다.
     */
    public Map<String, Object> insertRetry(RetryWrite row) {
        jdbc().update("""
                INSERT INTO exs_retry_log (
                    transaction_global_id,
                    external_transaction_id,
                    retry_status,
                    retry_count,
                    last_error_message,
                    next_retry_at,
                    created_by,
                    updated_by
                )
                VALUES (
                    :transactionGlobalId,
                    :externalTransactionId,
                    'REQUESTED',
                    0,
                    :reason,
                    NULL,
                    :requestUser,
                    :requestUser
                )
                """, new MapSqlParameterSource()
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("externalTransactionId", row.externalTransactionId())
                .addValue("reason", row.reason())
                .addValue("requestUser", row.requestUser()));
        return Map.of(
                "transactionGlobalId", row.transactionGlobalId(),
                "externalTransactionId", row.externalTransactionId(),
                "retryStatus", "REQUESTED",
                "reason", row.reason(),
                "requestUser", row.requestUser());
    }

    /**
     * 대외 재처리 요청 목록을 조회합니다.
     */
    public List<Map<String, Object>> findRetryRequests(int limit) {
        return jdbc().queryForList("""
                SELECT retry_log_id AS retryLogId,
                       transaction_global_id AS transactionGlobalId,
                       external_transaction_id AS externalTransactionId,
                       retry_status AS retryStatus,
                       retry_count AS retryCount,
                       last_error_message AS lastErrorMessage,
                       next_retry_at AS nextRetryAt,
                       created_by AS requestUser,
                       created_at AS requestedAt
                  FROM exs_retry_log
                 ORDER BY retry_log_id DESC
                 LIMIT :limit
                """, new MapSqlParameterSource("limit", limit));
    }

    /**
     * 대외 통제 정책을 upsert합니다.
     */
    public Map<String, Object> upsertControlPolicy(ControlPolicyWrite row) {
        jdbc().update("""
                INSERT INTO exs_control_policy (
                    institution_code,
                    control_type,
                    enabled_yn,
                    reason,
                    created_by,
                    updated_by
                )
                VALUES (
                    :institutionCode,
                    :controlType,
                    :enabledYn,
                    :reason,
                    :requestUser,
                    :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    enabled_yn = VALUES(enabled_yn),
                    reason = VALUES(reason),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """, new MapSqlParameterSource()
                .addValue("institutionCode", row.institutionCode())
                .addValue("controlType", row.controlType())
                .addValue("enabledYn", row.enabledYn())
                .addValue("reason", row.reason())
                .addValue("requestUser", row.requestUser()));
        return Map.of(
                "institutionCode", row.institutionCode(),
                "controlType", row.controlType(),
                "enabledYn", row.enabledYn(),
                "reason", row.reason(),
                "requestUser", row.requestUser(),
                "serverInstanceId", row.serverInstanceId());
    }

    /**
     * 대외 통제 정책 목록을 조회합니다.
     */
    public List<Map<String, Object>> findControlPolicies() {
        return jdbc().queryForList("""
                SELECT institution_code AS institutionCode,
                       control_type AS controlType,
                       enabled_yn AS enabledYn,
                       reason,
                       updated_by AS requestUser,
                       updated_at AS updatedAt
                  FROM exs_control_policy
                 ORDER BY institution_code, control_type
                """, Map.of());
    }

    /**
     * 대외 수신/송신 로그를 한 transaction으로 선저장합니다.
     */
    public Map<String, Object> saveExchangeLog(ExchangeLogWrite row) {
        return tx().execute(status -> {
            jdbc().update("""
                    INSERT INTO exs_transaction_log (
                        transaction_global_id,
                        external_transaction_id,
                        institution_code,
                        channel_code,
                        endpoint_code,
                        module_id,
                        was_id,
                        server_instance_id,
                        request_at,
                        direction,
                        http_method,
                        request_uri,
                        status,
                        result_code,
                        retryable_yn,
                        created_by,
                        updated_by
                    )
                    VALUES (
                        :transactionGlobalId,
                        :externalTransactionId,
                        :institutionCode,
                        :channelCode,
                        :endpointCode,
                        :moduleId,
                        :wasId,
                        :serverInstanceId,
                        :requestAt,
                        :direction,
                        :httpMethod,
                        :requestUri,
                        'PRE_SAVED',
                        'EXS_PRE_SAVED',
                        :retryableYn,
                        'EXS_FLOW',
                        'EXS_FLOW'
                    )
                    """, exchangeParams(row));
            jdbc().update("""
                    INSERT INTO exs_message_log (
                        transaction_global_id,
                        external_transaction_id,
                        direction,
                        message_summary,
                        payload_store_yn,
                        payload_ref,
                        created_by,
                        updated_by
                    )
                    VALUES (
                        :transactionGlobalId,
                        :externalTransactionId,
                        :direction,
                        :messageSummary,
                        'N',
                        NULL,
                        'EXS_FLOW',
                        'EXS_FLOW'
                    )
                    """, exchangeParams(row));
            return Map.of(
                    "transactionGlobalId", row.transactionGlobalId(),
                    "externalTransactionId", row.externalTransactionId(),
                    "direction", row.direction(),
                    "preSavedYn", "Y",
                    "status", "PRE_SAVED");
        });
    }

    private MapSqlParameterSource tokenParams(TokenWrite row) {
        return new MapSqlParameterSource()
                .addValue("authProfileCode", row.authProfileCode())
                .addValue("tokenKey", row.tokenKey())
                .addValue("tokenHash", row.tokenHash())
                .addValue("maskedToken", row.maskedToken())
                .addValue("tokenStatus", row.tokenStatus())
                .addValue("issuedAt", Timestamp.from(row.issuedAt()))
                .addValue("expireAt", Timestamp.from(row.expireAt()))
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("serverInstanceId", row.serverInstanceId())
                .addValue("requestUser", row.requestUser());
    }

    private MapSqlParameterSource exchangeParams(ExchangeLogWrite row) {
        return new MapSqlParameterSource()
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("externalTransactionId", row.externalTransactionId())
                .addValue("institutionCode", row.institutionCode())
                .addValue("channelCode", row.channelCode())
                .addValue("endpointCode", row.endpointCode())
                .addValue("moduleId", row.moduleId())
                .addValue("wasId", row.wasId())
                .addValue("serverInstanceId", row.serverInstanceId())
                .addValue("requestAt", Timestamp.from(row.requestAt()))
                .addValue("direction", row.direction())
                .addValue("httpMethod", row.httpMethod())
                .addValue("requestUri", row.requestUri())
                .addValue("retryableYn", row.retryableYn())
                .addValue("messageSummary", row.messageSummary());
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "EXS DB datasource가 비활성화되어 운영 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }

    private TransactionTemplate tx() {
        PlatformTransactionManager transactionManager = transactionManagerProvider.getIfAvailable();
        if (transactionManager == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "EXS transaction manager가 비활성화되어 선저장 로그를 사용할 수 없습니다.");
        }
        return new TransactionTemplate(transactionManager);
    }

    public record TokenWrite(
            String authProfileCode,
            String tokenKey,
            String tokenHash,
            String maskedToken,
            String tokenStatus,
            Instant issuedAt,
            Instant expireAt,
            String transactionGlobalId,
            String serverInstanceId,
            String requestUser) {
    }

    public record TokenEventWrite(
            String authProfileCode,
            String tokenKey,
            String eventType,
            String reason,
            String transactionGlobalId,
            String serverInstanceId,
            String requestUser) {
    }

    public record RetryWrite(
            String transactionGlobalId,
            String externalTransactionId,
            String reason,
            String requestUser) {
    }

    public record ControlPolicyWrite(
            String institutionCode,
            String controlType,
            String enabledYn,
            String reason,
            String requestUser,
            String serverInstanceId) {
    }

    public record ExchangeLogWrite(
            String transactionGlobalId,
            String externalTransactionId,
            String institutionCode,
            String channelCode,
            String endpointCode,
            String moduleId,
            String wasId,
            String serverInstanceId,
            Instant requestAt,
            String direction,
            String httpMethod,
            String requestUri,
            String retryableYn,
            String messageSummary) {
    }
}
