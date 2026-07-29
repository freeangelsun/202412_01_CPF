package com.cpf.core.common.database;

import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.common.logging.policy.JdbcLogPolicyRepository;
import com.cpf.core.common.logging.segment.CpfTransactionTimelineQueryFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfCoreRuntimeSqlPortabilityTest {

    @Test
    void logPolicyUsesJdbcMetadataAndStatementMaxRows() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DataSource dataSource = availableDataSource("cpf_log_policy");
        ObjectProvider<JdbcTemplate> jdbcProvider = provider(jdbc);
        ObjectProvider<DataSource> dataSourceProvider = provider(dataSource);
        when(jdbc.query(
                any(PreparedStatementCreator.class),
                any(ColumnMapRowMapper.class))).thenReturn(List.of());

        JdbcLogPolicyRepository repository =
                new JdbcLogPolicyRepository(jdbcProvider, dataSourceProvider);

        repository.findActivePolicy(
                LogPolicyTargetType.ONLINE_TRANSACTION,
                "REF-001");

        PreparedStatementCreator creator = capturedCreator(jdbc);
        PreparedStatement statement = prepareAndAssertPortable(creator);
        verify(statement).setMaxRows(1);
    }

    @Test
    void transactionTimelineUsesPortableWindowAggregationAndStatementMaxRows() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DataSource dataSource = availableDataSource("cpf_transaction_segment");
        when(jdbc.getDataSource()).thenReturn(dataSource);
        ObjectProvider<JdbcTemplate> jdbcProvider = provider(jdbc);
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("transactionId", "TX-001");
        when(jdbc.query(
                any(PreparedStatementCreator.class),
                any(ColumnMapRowMapper.class))).thenReturn(List.of(group));
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(
                Map.of(
                        "transactionId", "TX-001",
                        "moduleCode", "REF",
                        "transactionRole", "ENTRY")));

        CpfTransactionTimelineQueryFacade facade =
                new CpfTransactionTimelineQueryFacade(jdbcProvider);

        var result = facade.findGroups(Map.of(
                "transactionId", "TX-001",
                "limit", "7"));

        assertThat(result.available()).isTrue();
        assertThat(result.items()).singleElement()
                .satisfies(row -> {
                    assertThat(row).containsEntry("moduleFlowText", "REF");
                    assertThat(row).containsEntry("rolesText", "ENTRY");
                });
        PreparedStatementCreator creator = capturedCreator(jdbc);
        PreparedStatement statement = prepareAndAssertPortable(creator);
        verify(statement).setMaxRows(7);
    }

    private PreparedStatementCreator capturedCreator(JdbcTemplate jdbc) {
        ArgumentCaptor<PreparedStatementCreator> creator =
                ArgumentCaptor.forClass(PreparedStatementCreator.class);
        verify(jdbc).query(creator.capture(), any(ColumnMapRowMapper.class));
        return creator.getValue();
    }

    private PreparedStatement prepareAndAssertPortable(PreparedStatementCreator creator) throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        creator.createPreparedStatement(connection);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sql.capture());
        assertThat(sql.getValue().toUpperCase(Locale.ROOT))
                .doesNotContain(
                        "ON DUPLICATE KEY",
                        " LIMIT ",
                        "INFORMATION_SCHEMA",
                        "GROUP_CONCAT",
                        "SUBSTRING_INDEX",
                        "DATABASE()",
                        "TIMESTAMPADD")
                .doesNotContain("CONCAT(");
        return statement;
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private DataSource availableDataSource(String tableName) throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("cpfDB");
        when(connection.getSchema()).thenReturn(null);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getTables(
                eq("cpfDB"),
                isNull(),
                eq(tableName),
                aryEq(new String[]{"TABLE"}))).thenReturn(tables);
        when(tables.next()).thenReturn(true);
        return dataSource;
    }
}
