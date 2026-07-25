package com.cpf.reference.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * REF 업무 DB 기반 center-cut adapter 검증입니다.
 *
 * <p>기본 Gradle test에서는 로컬 DB를 건드리지 않도록 DB slice를 skip합니다.
 * {@code scripts/smoke-center-cut-adapter.ps1}는 안전한 검증 DB 정보를 환경변수로 주입해 이 테스트를 실행합니다.</p>
 */
class ReferenceCenterCutAdapterTest {
    private static final String ENABLED_ENV = "CPF_REF_CENTER_CUT_DB_TEST";
    private static final String DB_URL_ENV = "CPF_REF_CENTER_CUT_DB_URL";
    private static final String DB_USERNAME_ENV = "CPF_REF_CENTER_CUT_DB_USERNAME";
    private static final String DB_PASSWORD_ENV = "CPF_REF_CENTER_CUT_DB_PASSWORD";
    private static final String DB_DRIVER_ENV = "CPF_REF_CENTER_CUT_DB_DRIVER";

    @Test
    void handlerReturnsFailureWithSameTransactionAndSegmentContext() {
        ReferenceCenterCutHandler handler = new ReferenceCenterCutHandler();
        var target = new CpfCenterCutTarget(
                "REF-CENTER-CUT-FAIL",
                ReferenceCenterCutConstants.JOB_ID,
                "REF-BUSINESS-FAIL",
                java.time.LocalDate.of(2026, 7, 2),
                "{\"forceFail\":true}",
                "20260702100000000REFlocal010000001",
                "SEG-REF-PARENT-0001",
                "CC-REF-SEG-0001",
                0,
                CpfCenterCutStatus.READY);

        var result = handler.handle(target);

        assertThat(result.status()).isEqualTo(CpfCenterCutStatus.FAILED);
        assertThat(result.transactionSegmentId()).isEqualTo("CC-REF-SEG-0001");
        assertThat(target.transactionId()).isEqualTo("20260702100000000REFlocal010000001");
    }

    @Test
    void dbAdapterRunsWithFixtureWhenSafeDatabaseIsProvided() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv(ENABLED_ENV)),
                "안전한 테스트 DB가 명시된 경우에만 REF center-cut DB adapter smoke를 실행합니다.");

        DataSource dataSource = testDataSource();
        loadFixture(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        ReferenceCenterCutTargetRepository repository = new ReferenceCenterCutTargetRepository(jdbcTemplate);
        ReferenceCenterCutHandler handler = new ReferenceCenterCutHandler();
        repository.resetSampleTargetsForSmoke();
        var targets = repository.findReadyTargets(ReferenceCenterCutConstants.JOB_ID, 10);
        int success = 0;
        int failed = 0;
        int sequence = 0;
        for (CpfCenterCutTarget target : targets) {
            String segmentId = "CC-REF-20260702123000000-" + String.format("%07d", ++sequence);
            CpfCenterCutTarget running = target
                    .withExecutionContext(
                            target.transactionId() == null ? "20260702123000000REFlocal010000001" : target.transactionId(),
                            target.parentSegmentId() == null ? "SEG-REF-PARENT-0001" : target.parentSegmentId(),
                            segmentId)
                    .withStatus(CpfCenterCutStatus.RUNNING);
            repository.markRunning(running);
            CpfCenterCutResult result = handler.handle(running);
            repository.markResult(running, result);
            if (result.status() == CpfCenterCutStatus.SUCCESS) {
                success++;
            } else if (result.status() == CpfCenterCutStatus.FAILED) {
                failed++;
            }
        }

        assertThat(targets).hasSize(4);
        assertThat(success).isEqualTo(3);
        assertThat(failed).isEqualTo(1);
        assertThat(repository.countResultsByStatus(ReferenceCenterCutConstants.JOB_ID))
                .containsEntry("SUCCESS", 3L)
                .containsEntry("FAILED", 1L);
        assertThat(repository.findResultSnapshots(ReferenceCenterCutConstants.JOB_ID))
                .hasSize(4)
                .allSatisfy(row -> {
                    assertThat(row.get("transaction_id")).isNotNull();
                    assertThat(row.get("parent_segment_id")).isNotNull();
                    assertThat(row.get("transaction_segment_id")).isNotNull();
                });
    }

    private static DataSource testDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(envOrDefault(DB_DRIVER_ENV, "org.mariadb.jdbc.Driver"));
        dataSource.setUrl(requiredEnv(DB_URL_ENV));
        dataSource.setUsername(requiredEnv(DB_USERNAME_ENV));
        dataSource.setPassword(requiredEnv(DB_PASSWORD_ENV));
        return dataSource;
    }

    private static void loadFixture(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/ref_center_cut_fixture.sql"));
        }
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("REF center-cut DB 테스트 환경변수가 필요합니다. name=" + name);
        }
        return value;
    }
}
