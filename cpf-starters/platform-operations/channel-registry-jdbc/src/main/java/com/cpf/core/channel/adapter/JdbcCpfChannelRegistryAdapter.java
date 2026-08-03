package com.cpf.core.channel.adapter;

import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** cpfDB를 정본으로 사용하고 DB 미구성 시 안전한 로컬 기본 정책을 제공하는 어댑터입니다. */
public final class JdbcCpfChannelRegistryAdapter implements CpfChannelRegistryPort {
    private static final Logger log = LoggerFactory.getLogger(JdbcCpfChannelRegistryAdapter.class);

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final CpfVendorSqlCatalog sql;

    public JdbcCpfChannelRegistryAdapter(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this(jdbcTemplateProvider, new StandardEnvironment());
    }

    public JdbcCpfChannelRegistryAdapter(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = CpfVendorSqlCatalog.create(environment, "cpf");
    }

    @Override
    public CpfChannelPolicySnapshot loadSnapshot() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return CpfChannelPolicySnapshot.localDefault();
        }
        try {
            Map<String, CpfChannelDefinition> channels = new LinkedHashMap<>();
            jdbcTemplate.query(sql.required("channel-registry-find-all"), rs -> {
                CpfChannelDefinition definition = new CpfChannelDefinition(
                        rs.getString("channel_code"), rs.getString("channel_name"),
                        rs.getString("channel_type"), rs.getString("trust_level"),
                        yes(rs.getString("client_channel_yn")), yes(rs.getString("internal_channel_yn")),
                        yes(rs.getString("authentication_required_yn")), yes(rs.getString("signature_required_yn")),
                        yes(rs.getString("active_yn")), rs.getString("description"),
                        rs.getLong("policy_version"));
                channels.put(definition.channelCode(), definition);
            });
            List<CpfChannelExecutionPolicy> policies = jdbcTemplate.query(
                    sql.required("channel-policy-find-all"),
                    (rs, rowNum) -> new CpfChannelExecutionPolicy(
                    rs.getString("policy_key"), rs.getString("standard_execution_id"),
                    rs.getString("original_channel_code"), rs.getString("caller_channel_code"),
                    rs.getString("request_type"), yes(rs.getString("allowed_yn")),
                    yes(rs.getString("authentication_required_yn")), yes(rs.getString("signature_required_yn")),
                    rs.getInt("max_tps"), instant(rs.getTimestamp("effective_from")),
                    instant(rs.getTimestamp("effective_to")), yes(rs.getString("active_yn")),
                    rs.getLong("policy_version")));
            Long version = jdbcTemplate.queryForObject(
                    sql.required("channel-policy-version-current"), Long.class);
            if (channels.isEmpty()) {
                return CpfChannelPolicySnapshot.localDefault();
            }
            return new CpfChannelPolicySnapshot(version == null ? 0 : version, Instant.now(), channels, policies);
        } catch (DataAccessException ex) {
            log.warn("CPF 채널 정책 DB 조회에 실패해 로컬 기본 스냅샷을 사용합니다.", ex);
            return CpfChannelPolicySnapshot.localDefault();
        }
    }

    @Override
    public long saveChannel(CpfChannelDefinition channel, String actor, String reason) {
        JdbcTemplate jdbcTemplate = requiredJdbcTemplate();
        long version = nextVersion(jdbcTemplate, "CHANNEL", channel.channelCode(), actor, reason);
        jdbcTemplate.update(sql.required("channel-registry-upsert"),
                channel.channelCode(), channel.channelName(), channel.channelType(), channel.trustLevel(),
                yn(channel.clientChannel()), yn(channel.internalChannel()), yn(channel.authenticationRequired()),
                yn(channel.signatureRequired()), yn(channel.active()), channel.description(), version, actor, actor);
        return version;
    }

    @Override
    public long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason) {
        JdbcTemplate jdbcTemplate = requiredJdbcTemplate();
        long version = nextVersion(jdbcTemplate, "EXECUTION_POLICY", policy.policyKey(), actor, reason);
        jdbcTemplate.update(sql.required("channel-policy-upsert"),
                policy.policyKey(), policy.standardExecutionId(), policy.originalChannelCode(),
                policy.callerChannelCode(), policy.requestType(), yn(policy.allowed()),
                yn(policy.authenticationRequired()), yn(policy.signatureRequired()), policy.maxTps(),
                timestamp(policy.effectiveFrom()), timestamp(policy.effectiveTo()), yn(policy.active()),
                version, actor, actor);
        return version;
    }

    private long nextVersion(JdbcTemplate jdbcTemplate, String targetType, String targetKey, String actor, String reason) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    sql.required("channel-policy-version-insert"),
                    new String[]{"version_id"});
            statement.setString(1, targetType);
            statement.setString(2, targetKey);
            statement.setString(3, reason);
            statement.setString(4, actor);
            statement.setString(5, actor);
            statement.setString(6, actor);
            return statement;
        }, keyHolder);
        Number version = keyHolder.getKey();
        if (version == null || version.longValue() < 1) {
            throw new IllegalStateException("채널 정책 버전을 생성하지 못했습니다.");
        }
        return version.longValue();
    }

    private JdbcTemplate requiredJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("CPF 채널 정책 변경에는 cpfJdbcTemplate 구성이 필요합니다.");
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
