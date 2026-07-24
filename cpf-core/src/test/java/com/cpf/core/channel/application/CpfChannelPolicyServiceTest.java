package com.cpf.core.channel.application;

import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicyPackage;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfChannelPolicyServiceTest {

    @Test
    void 기본생성자는시작정책조회실패를숨기지않는다() {
        CpfChannelRegistryPort failingPort = failingPort();

        assertThatThrownBy(() -> new CpfChannelPolicyService(failingPort))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("정책 저장소");
    }

    @Test
    void 디비없는테스트는시작로드만명시적으로생략할수있다() {
        AtomicInteger loadCount = new AtomicInteger();
        CpfChannelRegistryPort failingPort = failingPort(loadCount);

        CpfChannelPolicyService service = new CpfChannelPolicyService(failingPort, false);

        assertThat(loadCount).hasValue(0);
        assertThat(service.snapshot().version()).isZero();
        assertThat(service.snapshot().channels()).isEmpty();
        assertThat(service.snapshot().policies()).isEmpty();
        assertThat(service.evaluate("OACCAC0001", "UNKNOWN", "WEB", "INQUIRY", true, false).allowed())
                .isFalse();
        assertThatThrownBy(service::refresh)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("정책 저장소");
        assertThat(loadCount).hasValue(1);
    }

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

    private CpfChannelRegistryPort failingPort() {
        return failingPort(new AtomicInteger());
    }

    private CpfChannelRegistryPort failingPort(AtomicInteger loadCount) {
        return new CpfChannelRegistryPort() {
            @Override
            public CpfChannelPolicySnapshot loadSnapshot() {
                loadCount.incrementAndGet();
                throw new IllegalStateException("정책 저장소 연결 실패");
            }

            @Override
            public long saveChannel(CpfChannelDefinition channel, String actor, String reason) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason) {
                throw new UnsupportedOperationException();
            }
        };
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
