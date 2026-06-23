package cpf.pfw.common.logging.policy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LogPolicyCacheTest {

    @Test
    void activeOverrideHasHighestPriority() {
        StubRepository repository = new StubRepository();
        repository.policy = new LogPolicyRow(
                10L,
                null,
                "ONLINE_TRANSACTION",
                "*",
                "INFO",
                "Y",
                "Y",
                "N",
                "N",
                "Y",
                "DEFAULT",
                "DB_POLICY");
        repository.override = new LogPolicyRow(
                10L,
                20L,
                "ONLINE_TRANSACTION",
                "ADM01TRN0010",
                "DEBUG",
                "N",
                "Y",
                "Y",
                "Y",
                "N",
                "DEBUG_MASK",
                "ADM_OVERRIDE");

        LogPolicyDecision decision = new LogPolicyCache(repository, new MockEnvironment())
                .resolve(LogPolicyTargetType.ONLINE_TRANSACTION, "ADM01TRN0010");

        assertThat(decision.resolvedSource()).isEqualTo("ADM_OVERRIDE");
        assertThat(decision.dbLogEnabled()).isFalse();
        assertThat(decision.requestBodySaveYn()).isEqualTo("Y");
        assertThat(decision.responseBodySaveYn()).isEqualTo("Y");
        assertThat(decision.errorStackSaveYn()).isEqualTo("N");
        assertThat(decision.overrideId()).isEqualTo(20L);
    }

    @Test
    void applicationDefaultIsUsedWhenDbPolicyIsMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.log-policy.default.file-log-level", "WARN")
                .withProperty("cpf.log-policy.default.db-log-enabled", "N")
                .withProperty("cpf.log-policy.default.request-body-save", "Y");

        LogPolicyDecision decision = new LogPolicyCache(new StubRepository(), environment)
                .resolve(LogPolicyTargetType.BATCH_JOB, "CPF_EDU_TASKLET_JOB");

        assertThat(decision.resolvedSource()).isEqualTo("APPLICATION_DEFAULT");
        assertThat(decision.fileLogLevel()).isEqualTo("WARN");
        assertThat(decision.dbLogEnabled()).isFalse();
        assertThat(decision.requestBodySave()).isTrue();
    }

    private static class StubRepository implements LogPolicyRepository {
        private LogPolicyRow policy;
        private LogPolicyRow override;

        @Override
        public Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
            return Optional.ofNullable(override);
        }

        @Override
        public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId) {
            return Optional.ofNullable(policy);
        }
    }
}
