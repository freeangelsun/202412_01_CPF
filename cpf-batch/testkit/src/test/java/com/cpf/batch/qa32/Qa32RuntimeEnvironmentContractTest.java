package com.cpf.batch.qa32;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Release 환경이 제공한 3 DB의 Spring Batch Repository와 Kafka-Remote 제거 상태를 실제로 조회합니다. */
@Tag("qa32-runtime")
class Qa32RuntimeEnvironmentContractTest {
    @Test void springBatchRepositoryExistsAndRemoteKafkaLedgerIsAbsentOnAllOfficialVendors() throws Exception {
        checkDatabase("ORACLE");
        checkDatabase("POSTGRESQL");
        checkDatabase("MARIADB");
    }

    private static void checkDatabase(String vendor) throws Exception {
        String url = required("CPF_QA32_" + vendor + "_JDBC_URL");
        String user = required("CPF_QA32_" + vendor + "_USER");
        String password = required("CPF_QA32_" + vendor + "_PASSWORD");
        try (var connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            statement.setQueryTimeout(15);
            try (var result = statement.executeQuery("SELECT COUNT(*) FROM BAT_SB_JOB_INSTANCE")) {
                assertTrue(result.next());
                assertTrue(result.getLong(1) >= 0);
            }
            try (var tables = connection.getMetaData().getTables(null, null, "BAT_REMOTE_MESSAGE_LEDGER", new String[] {"TABLE"})) {
                assertFalse(tables.next(), "retired BAT_REMOTE_MESSAGE_LEDGER must not exist on " + vendor);
            }
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        assertFalse(value == null || value.isBlank(), name + " is required for QA32 release verification");
        return value;
    }
}
