package com.cpf.common.biz.sequence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 MariaDB row lock으로 CMN 채번 중복 방지를 검증하는 선택형 통합 테스트입니다.
 *
 * <p>기본 단위 테스트에는 포함하지 않고, 로컬 DB 검증이 필요할 때
 * {@code CPF_CMN_DB_TEST=true} 환경변수를 주고 실행합니다.</p>
 */
class CmnSequenceServiceConcurrencyTest {
    private JdbcTemplate jdbcTemplate;
    private CmnSequenceService sequenceService;

    @BeforeEach
    void setUp() {
        DataSource dataSource = dataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
        sequenceService = new CmnSequenceService(
                provider(jdbcTemplate),
                provider(new TransactionTemplate(transactionManager)),
                Clock.systemDefaultZone());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "CPF_CMN_DB_TEST", matches = "true")
    void issueDoesNotDuplicateWhenManyThreadsUseSameSequenceKey() throws Exception {
        jdbcTemplate.update("DELETE FROM cmn_sequence_issue_log WHERE sequence_key = 'CMN_TEST_CONCURRENT'");
        jdbcTemplate.update("""
                INSERT INTO cmn_sequence (
                    sequence_key, business_area, business_key, sequence_kind, channel_code,
                    prefix, date_pattern, current_value, start_value, increment_by, min_value, max_value,
                    range_size, number_length, reset_cycle, reset_pattern, reset_timezone, last_reset_key,
                    log_enabled_yn, retention_days, description, use_yn, created_by, updated_by
                ) VALUES (
                    'CMN_TEST_CONCURRENT', 'CMN_TEST', 'CONCURRENT', 'ORDER_NO', 'WEB',
                    'TST', NULL, 0, 1, 1, 1, 999999,
                    1, 4, 'NONE', NULL, 'Asia/Seoul', NULL,
                    'Y', 365, '동시성 테스트용 채번', 'Y', 'TEST', 'TEST'
                )
                ON DUPLICATE KEY UPDATE
                    current_value = 0,
                    start_value = VALUES(start_value),
                    increment_by = VALUES(increment_by),
                    min_value = VALUES(min_value),
                    max_value = VALUES(max_value),
                    number_length = VALUES(number_length),
                    reset_cycle = VALUES(reset_cycle),
                    last_reset_key = NULL,
                    log_enabled_yn = 'Y',
                    use_yn = 'Y',
                    updated_by = 'TEST',
                    updated_at = CURRENT_TIMESTAMP
                """);

        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int index = i;
            tasks.add(() -> {
                startLatch.await();
                CmnSequenceIssueResult result = sequenceService.issue(new CmnSequenceIssueRequest(
                        null,
                        "CMN_TEST",
                        "CONCURRENT",
                        "ORDER_NO",
                        "WEB",
                        "TEST",
                        "TEST-" + index,
                        "TX-" + index,
                        "TRACE-" + index));
                return result.issuedNo();
            });
        }

        List<Future<String>> futures = tasks.stream().map(executor::submit).toList();
        startLatch.countDown();

        Set<String> issuedNumbers = new HashSet<>();
        for (Future<String> future : futures) {
            issuedNumbers.add(future.get());
        }
        executor.shutdownNow();

        assertThat(issuedNumbers).hasSize(threadCount);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT current_value FROM cmn_sequence WHERE sequence_key = 'CMN_TEST_CONCURRENT'",
                Long.class)).isEqualTo((long) threadCount);
    }

    private DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(requiredEnvironment("CMN_BUSINESS_DB_URL"));
        dataSource.setUsername(requiredEnvironment("CMN_BUSINESS_DB_USERNAME"));
        dataSource.setPassword(requiredEnvironment("CMN_BUSINESS_DB_PASSWORD"));
        return dataSource;
    }

    /**
     * 실 DB 테스트가 임의의 로컬 계정이나 비밀번호를 추정하지 않도록 필수 환경변수를 확인합니다.
     */
    private String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }

    private static <T> ObjectProvider<T> provider(T value) {
        return new ObjectProvider<>() {
            @Override
            public T getObject(Object... args) {
                return value;
            }

            @Override
            public T getIfAvailable() {
                return value;
            }

            @Override
            public T getIfUnique() {
                return value;
            }

            @Override
            public T getObject() {
                return value;
            }
        };
    }
}
