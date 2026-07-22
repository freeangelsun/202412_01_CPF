package cpf.pfw.channel.application;

import cpf.pfw.channel.api.CpfChannelRegistryPort;
import cpf.pfw.channel.model.CpfChannelDefinition;
import cpf.pfw.channel.model.CpfChannelExecutionPolicy;
import cpf.pfw.channel.model.CpfChannelPolicyPackage;
import cpf.pfw.channel.model.CpfChannelPolicySnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfChannelPolicyServiceTest {

    @Test
    void 구체적인거부정책이전체허용정책보다우선한다() {
        InMemoryPort port = new InMemoryPort(snapshot(List.of(
                policy("DEFAULT", "*", "ANY", "ANY", "*", true, false, false),
                policy("WEB.DENY", "OACCAC0001", "WEB", "WEB", "INQUIRY", false, false, false))));
        CpfChannelPolicyService service = new CpfChannelPolicyService(port);

        var decision = service.evaluate("OACCAC0001", "WEB", "WEB", "INQUIRY", true, false);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.matchedPolicyKey()).isEqualTo("WEB.DENY");
    }

    @Test
    void 채널자체인증요구도거래정책과함께검사한다() {
        CpfChannelPolicyService service = new CpfChannelPolicyService(
                new InMemoryPort(snapshot(List.of(policy("DEFAULT", "*", "ANY", "ANY", "*", true, false, false)))));

        assertThat(service.evaluate("OACCAC0001", "WEB", "WEB", "INQUIRY", false, false).allowed())
                .isFalse();
        assertThat(service.evaluate("OACCAC0001", "WEB", "WEB", "INQUIRY", true, false).allowed())
                .isTrue();
    }

    @Test
    void 미등록채널은전체허용정책이있어도거부한다() {
        CpfChannelPolicyService service = new CpfChannelPolicyService(
                new InMemoryPort(snapshot(List.of(policy("DEFAULT", "*", "ANY", "ANY", "*", true, false, false)))));

        var decision = service.evaluate("OACCAC0001", "UNKNOWN", "WEB", "INQUIRY", true, false);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).contains("최초 채널");
    }

    @Test
    void 내보낸패키지는Checksum변조를탐지한다() {
        CpfChannelPolicyService service = new CpfChannelPolicyService(
                new InMemoryPort(snapshot(List.of(policy("DEFAULT", "*", "ANY", "ANY", "*", true, false, false)))));
        CpfChannelPolicyPackage exported = service.exportPackage();
        CpfChannelPolicyPackage tampered = new CpfChannelPolicyPackage(
                exported.schemaVersion(), exported.exportedAt(), exported.channels(),
                List.of(policy("CHANGED", "*", "ANY", "ANY", "*", false, false, false)),
                exported.checksumSha256());

        assertThat(exported.hasValidChecksum()).isTrue();
        assertThat(tampered.hasValidChecksum()).isFalse();
        assertThatThrownBy(() -> service.importPackage(tampered, false, "tester", "변조 검사"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checksum");
    }

    @Test
    void 갱신전스냅샷은새정책저장후에도변하지않는다() {
        InMemoryPort port = new InMemoryPort(snapshot(List.of(
                policy("DEFAULT", "*", "ANY", "ANY", "*", true, false, false))));
        CpfChannelPolicyService service = new CpfChannelPolicyService(port);
        CpfChannelPolicySnapshot before = service.snapshot();

        service.savePolicy(
                policy("ADM.DENY", "OACCAC0001", "ADM", "ADM", "INQUIRY", false, false, false),
                "tester", "정책 저장 검증");

        assertThat(before.policies()).hasSize(1);
        assertThat(service.snapshot().policies()).hasSize(2);
        assertThatThrownBy(() -> before.channels().put("NEW", before.channels().get("WEB")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private CpfChannelPolicySnapshot snapshot(List<CpfChannelExecutionPolicy> policies) {
        Map<String, CpfChannelDefinition> channels = new LinkedHashMap<>();
        channels.put("ANY", channel("ANY", false));
        channels.put("WEB", channel("WEB", true));
        channels.put("ADM", channel("ADM", true));
        return new CpfChannelPolicySnapshot(1, Instant.now(), channels, policies);
    }

    private CpfChannelDefinition channel(String code, boolean authenticationRequired) {
        return new CpfChannelDefinition(code, code, "CLIENT", "EXTERNAL", true, false,
                authenticationRequired, false, true, "테스트 채널", 1);
    }

    private CpfChannelExecutionPolicy policy(
            String key,
            String executionId,
            String originalChannel,
            String callerChannel,
            String requestType,
            boolean allowed,
            boolean authenticationRequired,
            boolean signatureRequired) {
        return new CpfChannelExecutionPolicy(key, executionId, originalChannel, callerChannel,
                requestType, allowed, authenticationRequired, signatureRequired, 0,
                null, null, true, 1);
    }

    private static final class InMemoryPort implements CpfChannelRegistryPort {
        private CpfChannelPolicySnapshot snapshot;

        private InMemoryPort(CpfChannelPolicySnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public CpfChannelPolicySnapshot loadSnapshot() {
            return snapshot;
        }

        @Override
        public long saveChannel(CpfChannelDefinition channel, String actor, String reason) {
            Map<String, CpfChannelDefinition> channels = new LinkedHashMap<>(snapshot.channels());
            channels.put(channel.channelCode(), channel);
            snapshot = new CpfChannelPolicySnapshot(snapshot.version() + 1, Instant.now(), channels, snapshot.policies());
            return snapshot.version();
        }

        @Override
        public long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason) {
            List<CpfChannelExecutionPolicy> policies = new java.util.ArrayList<>(snapshot.policies());
            policies.removeIf(current -> current.policyKey().equals(policy.policyKey()));
            policies.add(policy);
            snapshot = new CpfChannelPolicySnapshot(snapshot.version() + 1, Instant.now(), snapshot.channels(), policies);
            return snapshot.version();
        }
    }
}
