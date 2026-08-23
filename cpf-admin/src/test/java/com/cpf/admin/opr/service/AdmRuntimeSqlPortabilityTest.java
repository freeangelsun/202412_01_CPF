package com.cpf.admin.opr.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmRuntimeSqlPortabilityTest {
    private static final List<String> PORTABLE_SERVICES = List.of(
            "AdmControlPlaneService.java",
            "AdmBreakGlassService.java",
            "AdmDownloadService.java",
            "AdmLogQueryService.java",
            "AdmObservabilityService.java",
            "AdmLogPolicyService.java");

    @Test
    void admRuntimeServicesDoNotEmbedVendorOnlySql() throws Exception {
        Path serviceRoot = Path.of("src/main/java/com/cpf/admin/opr/service");
        for (String service : PORTABLE_SERVICES) {
            String source = Files.readString(serviceRoot.resolve(service));
            assertThat(source)
                    .as(service)
                    .doesNotContain("ON DUPLICATE KEY")
                    .doesNotContain("LAST_INSERT_ID")
                    .doesNotContain("information_schema")
                    .doesNotContain("DATABASE()")
                    .doesNotContain("LIMIT ?")
                    .doesNotContain("NOW()")
                    .doesNotContain("CONCAT('%', ?, '%')");
        }

        String policySource = Files.readString(serviceRoot.resolve("AdmLogPolicyService.java"));
        assertThat(policySource)
                .contains("DuplicateKeyException")
                .contains("GeneratedKeyHolder")
                .contains("new String[]{\"override_id\"}")
                .contains("updatePolicyByKey");
    }

    @Test
    @SuppressWarnings("unchecked")
    void queryLimitUsesJdbcMaxRowsAndBindsOnlyBusinessParameters() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT value FROM sample WHERE code = ? ORDER BY value"))
                .thenReturn(statement);
        when(jdbcTemplate.query(any(PreparedStatementCreator.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    PreparedStatementCreator creator = invocation.getArgument(0);
                    creator.createPreparedStatement(connection);
                    return List.<Map<String, Object>>of();
                });

        List<Map<String, Object>> rows = AdmJdbcQueries.queryForList(
                jdbcTemplate,
                "SELECT value FROM sample WHERE code = ? ORDER BY value",
                List.of("A"),
                25);

        assertThat(rows).isEmpty();
        verify(statement).setObject(1, "A");
        verify(statement).setMaxRows(25);
    }

    @Test
    @SuppressWarnings("unchecked")
    void tableAvailabilityUsesJdbcMetadataWithinCurrentCatalogAndSchema() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);
        when(connection.getMetaData()).thenReturn(metadata);
        when(connection.getCatalog()).thenReturn("cpfDB");
        when(connection.getSchema()).thenReturn("cpf");
        when(metadata.getTables("cpfDB", "cpf", "OPS_LOG_POLICY", new String[] {"TABLE"}))
                .thenReturn(tables);
        when(tables.next()).thenReturn(true);
        when(tables.getString("TABLE_NAME")).thenReturn("OPS_LOG_POLICY");
        when(jdbcTemplate.execute(any(ConnectionCallback.class)))
                .thenAnswer(invocation -> {
                    ConnectionCallback<Boolean> callback = invocation.getArgument(0);
                    return callback.doInConnection(connection);
                });

        assertThat(AdmJdbcQueries.tableExists(jdbcTemplate, "OPS_LOG_POLICY")).isTrue();
        verify(metadata).getTables("cpfDB", "cpf", "OPS_LOG_POLICY", new String[] {"TABLE"});
    }
}
