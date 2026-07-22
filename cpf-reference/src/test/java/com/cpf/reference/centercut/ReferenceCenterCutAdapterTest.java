package cpf.xyz.centercut;

import cpf.pfw.common.batch.centercut.CpfCenterCutService;
import cpf.pfw.common.batch.centercut.CpfCenterCutStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * XYZ 업무 DB 기반 center-cut adapter 검증입니다.
 *
 * <p>기본 Gradle test에서는 로컬 DB를 건드리지 않도록 DB slice를 skip합니다.
 * {@code scripts/smoke-center-cut-adapter.ps1}는 안전한 검증 DB 정보를 환경변수로 주입해 이 테스트를 실행합니다.</p>
 */
class XyzCenterCutAdapterTest {
    private static final String ENABLED_ENV = "CPF_XYZ_CENTER_CUT_DB_TEST";
    private static final String DB_URL_ENV = "CPF_XYZ_CENTER_CUT_DB_URL";
    private static final String DB_USERNAME_ENV = "CPF_XYZ_CENTER_CUT_DB_USERNAME";
    private static final String DB_PASSWORD_ENV = "CPF_XYZ_CENTER_CUT_DB_PASSWORD";
    private static final String DB_DRIVER_ENV = "CPF_XYZ_CENTER_CUT_DB_DRIVER";

    @Test
    void handlerReturnsFailureForForceFailPayload() {
        XyzCenterCutHandler handler = new XyzCenterCutHandler();
        var target = new cpf.pfw.common.batch.centercut.CpfCenterCutTarget(
                "XYZ-CENTER-CUT-FAIL",
                XyzCenterCutConstants.JOB_ID,
                "XYZ-BUSINESS-FAIL",
                java.time.LocalDate.of(2026, 7, 2),
                "{\"forceFail\":true}",
                "20260702100000000XYZparent0000001",
                "20260702100000000XYZchild0000001",
                0,
                CpfCenterCutStatus.READY);

        var result = handler.handle(target);

        assertThat(result.status()).isEqualTo(CpfCenterCutStatus.FAILED);
        assertThat(result.childTransactionGlobalId()).isEqualTo("20260702100000000XYZchild0000001");
    }

    @Test
    void dbAdapterRunsWithFixtureWhenSafeDatabaseIsProvided() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv(ENABLED_ENV)),
                "안전한 테스트 DB가 명시된 경우에만 XYZ center-cut DB adapter smoke를 실행합니다.");

        DataSource dataSource = testDataSource();
        loadFixture(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        XyzCenterCutTargetRepository repository = new XyzCenterCutTargetRepository(jdbcTemplate);
        XyzCenterCutHandler handler = new XyzCenterCutHandler();
        AtomicLong sequence = new AtomicLong();
        CpfCenterCutService service = new CpfCenterCutService(() ->
                "20260702123000000XYZcentcut" + String.format("%07d", sequence.incrementAndGet()));

        repository.resetSampleTargetsForSmoke();
        var summary = service.execute(XyzCenterCutConstants.JOB_ID, 10, repository, handler);

        assertThat(summary.requestedCount()).isEqualTo(4);
        assertThat(summary.successCount()).isEqualTo(3);
        assertThat(summary.failedCount()).isEqualTo(1);
        assertThat(repository.countResultsByStatus(XyzCenterCutConstants.JOB_ID))
                .containsEntry("SUCCESS", 3L)
                .containsEntry("FAILED", 1L);
        assertThat(repository.findResultSnapshots(XyzCenterCutConstants.JOB_ID))
                .hasSize(4)
                .allSatisfy(row -> {
                    assertThat(row.get("parent_transaction_global_id")).isNotNull();
                    assertThat(row.get("child_transaction_global_id")).isNotNull();
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
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/xyz_center_cut_fixture.sql"));
        }
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("XYZ center-cut DB 테스트 환경변수가 필요합니다. name=" + name);
        }
        return value;
    }
}
