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
                .withProperty("cpf.log-policy.default.request-body-save", "Y")
                .withProperty("cpf.log-policy.default.response-body-save", "Y")
                .withProperty("cpf.log-policy.default.error-stack-save", "N")
                .withProperty("cpf.log-policy.default.masking-policy-key", "STRICT");

        LogPolicyDecision decision = new LogPolicyCache(new StubRepository(), environment)
                .resolve(LogPolicyTargetType.BATCH_JOB, "CPF_EDU_TASKLET_JOB");

        assertThat(decision.resolvedSource()).isEqualTo("APPLICATION_DEFAULT");
        assertThat(decision.fileLogLevel()).isEqualTo("WARN");
        assertThat(decision.dbLogEnabled()).isFalse();
        assertThat(decision.requestBodySave()).isTrue();
        assertThat(decision.responseBodySave()).isTrue();
        assertThat(decision.errorStackSave()).isFalse();
        assertThat(decision.maskingPolicyKey()).isEqualTo("STRICT");
    }

    @Test
    void cpfDefaultIsUsedWhenDbPolicyAndApplicationDefaultAreMissing() {
        LogPolicyDecision decision = new LogPolicyCache(new StubRepository(), new MockEnvironment())
                .resolve(LogPolicyTargetType.MODULE, "NO_POLICY_MODULE");

        assertThat(decision.resolvedSource()).isEqualTo("CPF_DEFAULT");
        assertThat(decision.fileLogLevel()).isEqualTo("INFO");
        assertThat(decision.dbLogEnabled()).isTrue();
        assertThat(decision.requestBodySave()).isFalse();
        assertThat(decision.responseBodySave()).isFalse();
        assertThat(decision.errorStackSave()).isTrue();
    }

    @Test
    void expiredOverrideFallsBackToDbPolicy() {
        WindowedOverrideRepository repository = new WindowedOverrideRepository();
        repository.policy = onlinePolicy();
        repository.override = onlineOverride();
        repository.startAt = LocalDateTime.now().minusHours(2);
        repository.endAt = LocalDateTime.now().minusHours(1);

        LogPolicyDecision decision = new LogPolicyCache(repository, new MockEnvironment())
                .resolve(LogPolicyTargetType.ONLINE_TRANSACTION, "ADM03LGP0014");

        assertThat(decision.resolvedSource()).isEqualTo("DB_POLICY");
        assertThat(decision.dbLogEnabled()).isTrue();
        assertThat(decision.requestBodySave()).isFalse();
        assertThat(decision.responseBodySave()).isFalse();
        assertThat(decision.errorStackSave()).isTrue();
    }

    @Test
    void futureOverrideFallsBackToDbPolicyUntilStartTime() {
        WindowedOverrideRepository repository = new WindowedOverrideRepository();
        repository.policy = onlinePolicy();
        repository.override = onlineOverride();
        repository.startAt = LocalDateTime.now().plusHours(1);
        repository.endAt = LocalDateTime.now().plusHours(2);

        LogPolicyDecision decision = new LogPolicyCache(repository, new MockEnvironment())
                .resolve(LogPolicyTargetType.ONLINE_TRANSACTION, "ADM03LGP0014");

        assertThat(decision.resolvedSource()).isEqualTo("DB_POLICY");
        assertThat(decision.dbLogEnabled()).isTrue();
        assertThat(decision.overrideId()).isNull();
        assertThat(decision.policyId()).isEqualTo(10L);
    }

    private LogPolicyRow onlinePolicy() {
        return new LogPolicyRow(
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
    }

    private LogPolicyRow onlineOverride() {
        return new LogPolicyRow(
                10L,
                30L,
                "ONLINE_TRANSACTION",
                "ADM03LGP0014",
                "DEBUG",
                "N",
                "Y",
                "Y",
                "Y",
                "N",
                "DEBUG_MASK",
                "ADM_OVERRIDE");
    }

    private static class StubRepository implements LogPolicyRepository {
        protected LogPolicyRow policy;
        protected LogPolicyRow override;

        @Override
        public Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
            return Optional.ofNullable(override);
        }

        @Override
        public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId) {
            return Optional.ofNullable(policy);
        }
    }

    private static class WindowedOverrideRepository extends StubRepository {
        private LocalDateTime startAt;
        private LocalDateTime endAt;

        @Override
        public Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
            if (override == null || startAt == null || endAt == null) {
                return Optional.empty();
            }
            if ((now.isEqual(startAt) || now.isAfter(startAt)) && (now.isEqual(endAt) || now.isBefore(endAt))) {
                return Optional.of(override);
            }
            return Optional.empty();
        }
    }
}
