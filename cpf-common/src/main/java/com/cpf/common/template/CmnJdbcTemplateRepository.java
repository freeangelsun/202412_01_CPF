package com.cpf.common.template;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated Disabled fail-closed compatibility shell. Product wiring must use
 * {@link CmnJdbcTemplateStore}/{@link CmnTemplateStore}; this class is deliberately not a Spring
 * bean and performs no JDBC operation, mutation, lookup, or audit write.
 */
@Deprecated(forRemoval = true)
public final class CmnJdbcTemplateRepository implements CmnTemplateRepository {
    private static UnsupportedOperationException removed() {
        return new UnsupportedOperationException(
                "CmnJdbcTemplateRepository is disabled; inject CmnTemplateStore instead");
    }

    public CmnJdbcTemplateRepository(DataSource ignored) {
        throw removed();
    }

    @Override
    public Optional<CmnTemplateDefinition> findActive(String templateCode, String channel) {
        throw removed();
    }

    @Override
    public CmnTemplateRecord createDraft(CmnTemplateDefinition definition, String actor) {
        throw removed();
    }

    @Override
    public CmnTemplateRecord approve(String templateCode, long version, String channel, String approver) {
        throw removed();
    }

    @Override
    public CmnTemplateRecord retire(String templateCode, long version, String channel, String actor) {
        throw removed();
    }

    @Override
    public Optional<CmnTemplateRecord> findVersion(String templateCode, long version, String channel) {
        throw removed();
    }

    @Override
    public List<CmnTemplateRecord> history(String templateCode, String channel, int limit) {
        throw removed();
    }
}
