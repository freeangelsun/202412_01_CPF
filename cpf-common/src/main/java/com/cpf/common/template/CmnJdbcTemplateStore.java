package com.cpf.common.template;

import com.cpf.common.persistence.CpfCommonSqlResourceLoader;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Official cpfDB JDBC implementation for versioned common-template lifecycle and append-only audit. */
public final class CmnJdbcTemplateStore implements CmnTemplateStore {
    private static final int MAX_AUDIT_ROWS = 500;
    private final JdbcTemplate jdbc;

    public CmnJdbcTemplateStore(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<CmnTemplateDefinition> findActive(String templateCode, String channel) {
        String code = required(templateCode, "templateCode", 100);
        String channelCode = required(channel, "channel", 30);
        List<Row> rows = jdbc.query(connection -> {
            var statement = connection.prepareStatement(CpfCommonSqlResourceLoader.load("template/select-active.sql"));
            statement.setString(1, code);
            statement.setString(2, channelCode);
            statement.setMaxRows(2);
            return statement;
        }, (rs, rowNum) -> row(rs));
        if (rows.size() > 1) {
            throw new IllegalStateException("Multiple active template versions: " + code + "/" + channelCode);
        }
        return rows.stream().findFirst().map(this::definition);
    }

    @Override
    public Optional<CmnTemplateVersion> findVersion(String templateCode, String channel, long version) {
        String code = required(templateCode, "templateCode", 100);
        String channelCode = required(channel, "channel", 30);
        requireVersionNumber(version);
        return jdbc.query(CpfCommonSqlResourceLoader.load("template/select-version.sql"),
                (rs, rowNum) -> row(rs), code, version, channelCode).stream().findFirst().map(this::version);
    }

    @Override
    public List<CmnTemplateAuditEntry> auditHistory(
            String templateCode, String channel, long version, int limit) {
        String code = required(templateCode, "templateCode", 100);
        String channelCode = required(channel, "channel", 30);
        requireVersionNumber(version);
        int bounded = Math.max(1, Math.min(limit, MAX_AUDIT_ROWS));
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(CpfCommonSqlResourceLoader.load("template/select-audit.sql"));
            statement.setString(1, code);
            statement.setLong(2, version);
            statement.setString(3, channelCode);
            statement.setMaxRows(bounded);
            return statement;
        }, (rs, rowNum) -> new CmnTemplateAuditEntry(
                rs.getString("audit_id"), rs.getString("template_code"), rs.getLong("template_version"),
                rs.getString("channel_code"), rs.getString("action_type"), rs.getString("request_user"),
                rs.getString("request_reason"), rs.getString("before_status"), rs.getString("after_status"),
                rs.getLong("revision_no"), rs.getTimestamp("occurred_at").toInstant()));
    }

    @Override
    public CmnTemplateVersion createDraft(CmnTemplateDefinition definition, String actor, String reason) {
        if (definition.active()) throw new IllegalArgumentException("A new draft cannot be active");
        String operator = required(actor, "actor", 100);
        String auditReason = required(reason, "reason", 500);
        try {
            int inserted = jdbc.update(CpfCommonSqlResourceLoader.load("template/insert-draft.sql"),
                    definition.templateCode(), definition.version(), definition.channel(), definition.body(),
                    encodeVariables(definition.allowedVariables()), operator, operator);
            if (inserted != 1) throw new IllegalStateException("template draft insert affected rows: " + inserted);
        } catch (DuplicateKeyException ex) {
            throw new CmnTemplateConflictException(CmnTemplateConflictException.Type.VERSION_EXISTS,
                    "Template version already exists: " + definition.templateCode() + "/" + definition.channel()
                            + "/" + definition.version());
        }
        insertAudit("CREATE_DRAFT", definition.templateCode(), definition.version(), definition.channel(),
                operator, auditReason, null, "DRAFT", 0);
        return requireVersion(definition.templateCode(), definition.channel(), definition.version());
    }

    @Override
    public CmnTemplateVersion approve(String templateCode, String channel, long version, long expectedRevision,
            String actor, String reason) {
        String code = required(templateCode, "templateCode", 100);
        String channelCode = required(channel, "channel", 30);
        String operator = required(actor, "actor", 100);
        String auditReason = required(reason, "reason", 500);
        requireVersionNumber(version);
        requireRevision(expectedRevision);
        List<Row> locked = lockHistory(code, channelCode);
        Row target = locked.stream().filter(r -> r.version() == version).findFirst()
                .orElseThrow(() -> new CmnTemplateConflictException(
                        CmnTemplateConflictException.Type.NOT_FOUND, "Template version not found"));
        if (!"DRAFT".equals(target.statusCode())) {
            throw new CmnTemplateConflictException(CmnTemplateConflictException.Type.INVALID_STATE,
                    "Only DRAFT templates can be approved");
        }
        if (target.revision() != expectedRevision) {
            throw new CmnTemplateConflictException(CmnTemplateConflictException.Type.REVISION_CONFLICT,
                    "Template approval revision conflict");
        }
        if (target.createdBy().equals(operator)) {
            throw new CmnTemplateConflictException(CmnTemplateConflictException.Type.INVALID_STATE,
                    "template creator and approver must be different");
        }
        for (Row active : locked) {
            if ("Y".equalsIgnoreCase(active.activeYn()) && "APPROVED".equals(active.statusCode())) {
                int retired = jdbc.update(CpfCommonSqlResourceLoader.load("template/supersede.sql"),
                        operator, code, active.version(), channelCode, active.revision());
                if (retired != 1) throw new CmnTemplateConflictException(
                        CmnTemplateConflictException.Type.REVISION_CONFLICT, "Active template changed concurrently");
                insertAudit("SUPERSEDE", code, active.version(), channelCode, operator, auditReason,
                        "APPROVED", "RETIRED", active.revision() + 1);
            }
        }
        int updated = jdbc.update(CpfCommonSqlResourceLoader.load("template/approve.sql"),
                operator, operator, code, version, channelCode, expectedRevision);
        if (updated != 1) throw new CmnTemplateConflictException(
                CmnTemplateConflictException.Type.REVISION_CONFLICT, "Template approval revision conflict");
        insertAudit("APPROVE", code, version, channelCode, operator, auditReason,
                "DRAFT", "APPROVED", expectedRevision + 1);
        return requireVersion(code, channelCode, version);
    }

    @Override
    public CmnTemplateVersion retire(String templateCode, String channel, long version, long expectedRevision,
            String actor, String reason) {
        String code = required(templateCode, "templateCode", 100);
        String channelCode = required(channel, "channel", 30);
        String operator = required(actor, "actor", 100);
        String auditReason = required(reason, "reason", 500);
        requireVersionNumber(version);
        requireRevision(expectedRevision);
        Row current = lockHistory(code, channelCode).stream().filter(r -> r.version() == version).findFirst()
                .orElseThrow(() -> new CmnTemplateConflictException(
                        CmnTemplateConflictException.Type.NOT_FOUND, "Template version not found"));
        if (!"APPROVED".equals(current.statusCode()) || !"Y".equalsIgnoreCase(current.activeYn())) {
            throw new CmnTemplateConflictException(CmnTemplateConflictException.Type.INVALID_STATE,
                    "Only the active APPROVED template can be retired");
        }
        if (current.revision() != expectedRevision) throw new CmnTemplateConflictException(
                CmnTemplateConflictException.Type.REVISION_CONFLICT, "Template retirement revision conflict");
        int updated = jdbc.update(CpfCommonSqlResourceLoader.load("template/retire.sql"),
                operator, code, version, channelCode, expectedRevision);
        if (updated != 1) throw new CmnTemplateConflictException(
                CmnTemplateConflictException.Type.REVISION_CONFLICT, "Template retirement revision conflict");
        insertAudit("RETIRE", code, version, channelCode, operator, auditReason,
                "APPROVED", "RETIRED", expectedRevision + 1);
        return requireVersion(code, channelCode, version);
    }

    private List<Row> lockHistory(String code, String channel) {
        return jdbc.query(CpfCommonSqlResourceLoader.load("template/lock-history.sql"),
                (rs, rowNum) -> row(rs), code, channel);
    }

    private CmnTemplateVersion requireVersion(String code, String channel, long version) {
        return findVersion(code, channel, version).orElseThrow(() -> new CmnTemplateConflictException(
                CmnTemplateConflictException.Type.NOT_FOUND,
                "Template version not found: " + code + "/" + channel + "/" + version));
    }

    private CmnTemplateVersion version(Row row) {
        return new CmnTemplateVersion(definition(row),
                CmnTemplateLifecycleStatus.valueOf(row.statusCode()), row.revision());
    }

    private CmnTemplateDefinition definition(Row row) {
        return new CmnTemplateDefinition(row.templateCode(), row.version(), row.channel(), row.body(),
                decodeVariables(row.allowedVariables()),
                "Y".equalsIgnoreCase(row.activeYn()) && "APPROVED".equals(row.statusCode()));
    }

    private Row row(ResultSet rs) throws SQLException {
        return new Row(rs.getString("template_code"), rs.getLong("template_version"), rs.getString("channel_code"),
                rs.getString("template_body"), rs.getString("allowed_variables"), rs.getString("status_code"),
                rs.getString("active_yn"), rs.getLong("revision_no"), rs.getString("created_by"));
    }

    private void insertAudit(String action, String code, long version, String channel, String actor, String reason,
            String beforeStatus, String afterStatus, long revision) {
        int inserted = jdbc.update(CpfCommonSqlResourceLoader.load("template/insert-audit.sql"),
                UUID.randomUUID().toString(), code, version, channel, action, actor, reason,
                beforeStatus, afterStatus, revision);
        if (inserted != 1) throw new IllegalStateException("template audit insert affected rows: " + inserted);
    }

    static String encodeVariables(Set<String> variables) {
        if (variables == null || variables.isEmpty()) return "-";
        return String.join(",", new TreeSet<>(variables));
    }

    static Set<String> decodeVariables(String encoded) {
        if (encoded == null || encoded.isBlank() || "-".equals(encoded)) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Arrays.stream(encoded.split(",")).map(String::trim).filter(value -> !value.isEmpty()).forEach(result::add);
        return Set.copyOf(result);
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(field + " exceeds max length " + maxLength);
        return normalized;
    }

    private static void requireVersionNumber(long version) {
        if (version <= 0) throw new IllegalArgumentException("version must be greater than zero");
    }

    private static void requireRevision(long revision) {
        if (revision < 0) throw new IllegalArgumentException("expectedRevision must not be negative");
    }

    private record Row(String templateCode, long version, String channel, String body, String allowedVariables,
            String statusCode, String activeYn, long revision, String createdBy) { }
}
