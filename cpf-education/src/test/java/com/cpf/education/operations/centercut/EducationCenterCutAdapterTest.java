package com.cpf.education.operations.centercut;
import com.cpf.batch.api.centercut.CpfCenterCutResult;
import com.cpf.batch.api.centercut.CpfCenterCutStatus;
import com.cpf.batch.api.centercut.CpfCenterCutTarget;
import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.jdbc.CpfVendorSqlCatalogs;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EDU 업무 DB 기반 center-cut adapter 검증입니다.
 *
 * <p>기본 Gradle test에서는 로컬 DB를 건드리지 않도록 DB slice를 skip합니다.
 * {@code scripts/smoke-center-cut-adapter.ps1}는 공식 Provision/Install/Test Seed가 끝난
 * 격리 검증 DB 정보를 환경변수로 주입해 이 테스트를 실행합니다.</p>
 */
class EducationCenterCutAdapterTest {
    private static final String ENABLED_ENV = "CPF_EDU_CENTER_CUT_DB_TEST";
    private static final String DB_URL_ENV = "CPF_EDU_CENTER_CUT_DB_URL";
    private static final String DB_USERNAME_ENV = "CPF_EDU_CENTER_CUT_DB_USERNAME";
    private static final String DB_PASSWORD_ENV = "CPF_EDU_CENTER_CUT_DB_PASSWORD";
    private static final String DB_DRIVER_ENV = "CPF_EDU_CENTER_CUT_DB_DRIVER";

    @Test
    void handlerReturnsFailureWithSameTransactionAndSegmentContext() {
        EducationCenterCutHandler handler = new EducationCenterCutHandler();
        var target = new CpfCenterCutTarget(
                "EDU-CENTER-CUT-FAIL",
                EducationCenterCutConstants.JOB_ID,
                "EDU-BUSINESS-FAIL",
                java.time.LocalDate.of(2026, 7, 2),
                "{\"forceFail\":true}",
                "20260702100000000EDUlocal010000001",
                "SEG-EDU-PARENT-0001",
                "CC-EDU-SEG-0001",
                0,
                CpfCenterCutStatus.READY);

        var result = handler.handle(target);

        assertThat(result.status()).isEqualTo(CpfCenterCutStatus.FAILED);
        assertThat(result.transactionSegmentId()).isEqualTo("CC-EDU-SEG-0001");
        assertThat(target.transactionId()).isEqualTo("20260702100000000EDUlocal010000001");
    }

    @Test
    void dbAdapterRunsWithFixtureWhenSafeDatabaseIsProvided() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv(ENABLED_ENV)),
                "안전한 테스트 DB가 명시된 경우에만 EDU center-cut DB adapter smoke를 실행합니다.");

        DataSource dataSource = testDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        EducationCenterCutTargetRepository repository =
                new EducationCenterCutTargetRepository(jdbcTemplate, vendorCatalog());
        EducationCenterCutHandler handler = new EducationCenterCutHandler();
        repository.resetSampleTargetsForSmoke();
        var targets = repository.findReadyTargets(EducationCenterCutConstants.JOB_ID, 10);
        int success = 0;
        int failed = 0;
        int sequence = 0;
        for (CpfCenterCutTarget target : targets) {
            String segmentId = "CC-EDU-20260702123000000-" + String.format("%07d", ++sequence);
            CpfCenterCutTarget running = target
                    .withExecutionContext(
                            target.transactionId() == null ? "20260702123000000EDUlocal010000001" : target.transactionId(),
                            target.parentSegmentId() == null ? "SEG-EDU-PARENT-0001" : target.parentSegmentId(),
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
        assertThat(repository.countResultsByStatus(EducationCenterCutConstants.JOB_ID))
                .containsEntry("SUCCESS", 3L)
                .containsEntry("FAILED", 1L);
        assertThat(repository.findResultSnapshots(EducationCenterCutConstants.JOB_ID))
                .hasSize(4)
                .allSatisfy(row -> {
                    assertThat(row.get("transaction_id")).isNotNull();
                    assertThat(row.get("parent_segment_id")).isNotNull();
                    assertThat(row.get("transaction_segment_id")).isNotNull();
                });
    }

    private static DataSource testDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(envOrDefault(DB_DRIVER_ENV, selectedDriverClassName()));
        dataSource.setUrl(requiredEnv(DB_URL_ENV));
        dataSource.setUsername(requiredEnv(DB_USERNAME_ENV));
        dataSource.setPassword(requiredEnv(DB_PASSWORD_ENV));
        return dataSource;
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("EDU center-cut DB 테스트 환경변수가 필요합니다. name=" + name);
        }
        return value;
    }

    private static CpfVendorSqlCatalog vendorCatalog() {
        CpfDatabaseVendor vendor = selectedVendor();
        String configuredRoot = System.getProperty("cpf.db.resource-root");
        Path resourceRoot = configuredRoot == null || configuredRoot.isBlank()
                ? findRepositoryVendorRoot(vendor.id())
                : Path.of(configuredRoot).toAbsolutePath().normalize();
        return CpfVendorSqlCatalogs.fromPack(vendor, "ref", resourceRoot);
    }

    private static CpfDatabaseVendor selectedVendor() {
        String explicit = System.getenv("CPF_EDU_CENTER_CUT_DB_VENDOR");
        if (explicit != null && !explicit.isBlank()) {
            return CpfDatabaseVendor.from(explicit);
        }
        String configured = System.getProperty("cpf.db.vendor");
        if (configured != null && !configured.isBlank()) {
            return CpfDatabaseVendor.from(configured);
        }
        String driver = System.getenv(DB_DRIVER_ENV);
        if (driver != null && !driver.isBlank()) {
            return CpfDatabaseVendor.fromDriverClassName(driver);
        }
        return CpfDatabaseVendor.values()[0];
    }

    private static String selectedDriverClassName() {
        return selectedVendor().driverClassName();
    }

    private static Path findRepositoryVendorRoot(String vendor) {
        try (Stream<Path> candidates = Stream.of(
                Path.of("cpf-tools", "db", "vendor", vendor),
                Path.of("..", "cpf-tools", "db", "vendor", vendor))) {
            return candidates
                    .map(path -> path.toAbsolutePath().normalize())
                    .filter(Files::isDirectory)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "중앙 Vendor Pack 경로를 찾을 수 없습니다. vendor=" + vendor));
        }
    }
}
