package com.cpf.batch.qa32;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Release 환경이 제공한 3 DB JobRepository와 Kafka Broker를 실제로 조회하는 fail-closed Test입니다. */
@Tag("qa32-runtime")
class Qa32RuntimeEnvironmentContractTest {
    @Test void springBatchRepositoryExistsOnAllOfficialVendors() throws Exception {
        verifyDb("ORACLE", "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
        verifyDb("POSTGRESQL", "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
        verifyDb("MARIADB", "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
    }

    @Test void kafkaBrokerAndRemoteTopicsAreReachable() throws Exception {
        String bootstrap = required("CPF_QA32_KAFKA_BOOTSTRAP");
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        try (AdminClient client = AdminClient.create(properties)) {
            var names = client.listTopics().names().get(15, TimeUnit.SECONDS);
            assertTrue(names.contains("cpf.batch.remote.requests.v1"), "request topic missing");
            assertTrue(names.contains("cpf.batch.remote.replies.v1"), "reply topic missing");
        }
    }

    private static void verifyDb(String vendor, String sql) throws Exception {
        String url = required("CPF_QA32_" + vendor + "_JDBC_URL");
        String user = required("CPF_QA32_" + vendor + "_USER");
        String password = required("CPF_QA32_" + vendor + "_PASSWORD");
        try (var connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            statement.setQueryTimeout(15);
            try (var result = statement.executeQuery(sql)) {
                assertTrue(result.next());
                assertTrue(result.getLong(1) >= 0);
            }
        }
    }
    private static String required(String name) {
        String value = System.getenv(name);
        assertFalse(value == null || value.isBlank(), name + " is required for QA32 release verification");
        return value;
    }
}
