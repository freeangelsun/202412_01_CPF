package cpf.pfw.channel.adapter;

import cpf.pfw.channel.api.CpfChannelRegistryPort;
import cpf.pfw.channel.model.CpfChannelDefinition;
import cpf.pfw.channel.model.CpfChannelExecutionPolicy;
import cpf.pfw.channel.model.CpfChannelPolicySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** pfwDB를 정본으로 사용하고 DB 미구성 시 안전한 로컬 기본 정책을 제공하는 어댑터입니다. */
public final class JdbcCpfChannelRegistryAdapter implements CpfChannelRegistryPort {
    private static final Logger log = LoggerFactory.getLogger(JdbcCpfChannelRegistryAdapter.class);

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    public JdbcCpfChannelRegistryAdapter(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Override
    public CpfChannelPolicySnapshot loadSnapshot() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return CpfChannelPolicySnapshot.localDefault();
        }
        try {
            Map<String, CpfChannelDefinition> channels = new LinkedHashMap<>();
            jdbcTemplate.query("""
                    SELECT channel_code, channel_name, channel_type, trust_level,
                           client_channel_yn, internal_channel_yn, authentication_required_yn,
                           signature_required_yn, active_yn, description, policy_version
                    FROM pfw_channel_registry
                    ORDER BY channel_code
                    """, rs -> {
                CpfChannelDefinition definition = new CpfChannelDefinition(
                        rs.getString("channel_code"), rs.getString("channel_name"),
                        rs.getString("channel_type"), rs.getString("trust_level"),
                        yes(rs.getString("client_channel_yn")), yes(rs.getString("internal_channel_yn")),
                        yes(rs.getString("authentication_required_yn")), yes(rs.getString("signature_required_yn")),
                        yes(rs.getString("active_yn")), rs.getString("description"),
                        rs.getLong("policy_version"));
                channels.put(definition.channelCode(), definition);
            });
            List<CpfChannelExecutionPolicy> policies = jdbcTemplate.query("""
                    SELECT policy_key, standard_execution_id, original_channel_code, caller_channel_code,
                           request_type, allowed_yn, authentication_required_yn, signature_required_yn,
                           max_tps, effective_from, effective_to, active_yn, policy_version
                    FROM pfw_channel_execution_policy
                    ORDER BY policy_key
                    """, (rs, rowNum) -> new CpfChannelExecutionPolicy(
                    rs.getString("policy_key"), rs.getString("standard_execution_id"),
                    rs.getString("original_channel_code"), rs.getString("caller_channel_code"),
                    rs.getString("request_type"), yes(rs.getString("allowed_yn")),
                    yes(rs.getString("authentication_required_yn")), yes(rs.getString("signature_required_yn")),
                    rs.getInt("max_tps"), instant(rs.getTimestamp("effective_from")),
                    instant(rs.getTimestamp("effective_to")), yes(rs.getString("active_yn")),
                    rs.getLong("policy_version")));
            Long version = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(version_id), 0) FROM pfw_channel_policy_version", Long.class);
            if (channels.isEmpty()) {
                return CpfChannelPolicySnapshot.localDefault();
            }
            return new CpfChannelPolicySnapshot(version == null ? 0 : version, Instant.now(), channels, policies);
        } catch (DataAccessException ex) {
            log.warn("PFW 채널 정책 DB 조회에 실패해 로컬 기본 스냅샷을 사용합니다.", ex);
            return CpfChannelPolicySnapshot.localDefault();
        }
    }

    @Override
    public long saveChannel(CpfChannelDefinition channel, String actor, String reason) {
        JdbcTemplate jdbcTemplate = requiredJdbcTemplate();
        long version = nextVersion(jdbcTemplate, "CHANNEL", channel.channelCode(), actor, reason);
        jdbcTemplate.update("""
                INSERT INTO pfw_channel_registry (
                    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
                    internal_channel_yn, authentication_required_yn, signature_required_yn,
                    active_yn, description, policy_version, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    channel_name = VALUES(channel_name), channel_type = VALUES(channel_type),
                    trust_level = VALUES(trust_level), client_channel_yn = VALUES(client_channel_yn),
                    internal_channel_yn = VALUES(internal_channel_yn),
                    authentication_required_yn = VALUES(authentication_required_yn),
                    signature_required_yn = VALUES(signature_required_yn), active_yn = VALUES(active_yn),
                    description = VALUES(description), policy_version = VALUES(policy_version),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, channel.channelCode(), channel.channelName(), channel.channelType(), channel.trustLevel(),
                yn(channel.clientChannel()), yn(channel.internalChannel()), yn(channel.authenticationRequired()),
                yn(channel.signatureRequired()), yn(channel.active()), channel.description(), version, actor, actor);
        return version;
    }

    @Override
    public long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason) {
        JdbcTemplate jdbcTemplate = requiredJdbcTemplate();
        long version = nextVersion(jdbcTemplate, "EXECUTION_POLICY", policy.policyKey(), actor, reason);
        jdbcTemplate.update("""
                INSERT INTO pfw_channel_execution_policy (
                    policy_key, standard_execution_id, original_channel_code, caller_channel_code,
                    request_type, allowed_yn, authentication_required_yn, signature_required_yn,
                    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    standard_execution_id = VALUES(standard_execution_id),
                    original_channel_code = VALUES(original_channel_code),
                    caller_channel_code = VALUES(caller_channel_code), request_type = VALUES(request_type),
                    allowed_yn = VALUES(allowed_yn),
                    authentication_required_yn = VALUES(authentication_required_yn),
                    signature_required_yn = VALUES(signature_required_yn), max_tps = VALUES(max_tps),
                    effective_from = VALUES(effective_from), effective_to = VALUES(effective_to),
                    active_yn = VALUES(active_yn), policy_version = VALUES(policy_version),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, policy.policyKey(), policy.standardExecutionId(), policy.originalChannelCode(),
                policy.callerChannelCode(), policy.requestType(), yn(policy.allowed()),
                yn(policy.authenticationRequired()), yn(policy.signatureRequired()), policy.maxTps(),
                timestamp(policy.effectiveFrom()), timestamp(policy.effectiveTo()), yn(policy.active()),
                version, actor, actor);
        return version;
    }

    private long nextVersion(JdbcTemplate jdbcTemplate, String targetType, String targetKey, String actor, String reason) {
        jdbcTemplate.update("""
                INSERT INTO pfw_channel_policy_version (
                    change_type, target_key, change_reason, applied_by, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, targetType, targetKey, reason, actor, actor, actor);
        Long version = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (version == null || version < 1) {
            throw new IllegalStateException("채널 정책 버전을 생성하지 못했습니다.");
        }
        return version;
    }

    private JdbcTemplate requiredJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("PFW 채널 정책 변경에는 pfwJdbcTemplate 구성이 필요합니다.");
        }
        return jdbcTemplate;
    }

    private static boolean yes(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private static String yn(boolean value) {
        return value ? "Y" : "N";
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private static Timestamp timestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }
}
