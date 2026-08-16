package com.cpf.common.template;

import com.cpf.common.runtime.cache.CpfCommonCacheRefreshPublisher;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CmnTemplateManagementServiceTest {
    @Test
    void createsApprovesAndRetiresWithDurableRefresh() {
        FakeStore store = new FakeStore();
        CpfCommonCacheRefreshPublisher publisher = mock(CpfCommonCacheRefreshPublisher.class);
        CmnTemplateManagementService service = new CmnTemplateManagementService(store, publisher);
        CmnTemplateDefinition draft = new CmnTemplateDefinition(
                "WELCOME", 1, "SMS", "Hello ${name}", Set.of("name"), false);

        CmnTemplateVersion created = service.createDraft(draft, "operator", "initial registration");
        CmnTemplateVersion approved = service.approve("WELCOME", "SMS", 1, created.revision(),
                "approver", "four-eyes approval");
        CmnTemplateVersion retired = service.retire("WELCOME", "SMS", 1, approved.revision(),
                "approver", "replacement deployed");

        assertThat(created.status()).isEqualTo(CmnTemplateLifecycleStatus.DRAFT);
        assertThat(approved.definition().active()).isTrue();
        assertThat(retired.status()).isEqualTo(CmnTemplateLifecycleStatus.RETIRED);
        verify(publisher).publishRequired("commonTemplate", "CREATE_DRAFT", "WELCOME:SMS:1:0", "operator");
        verify(publisher).publishRequired("commonTemplate", "APPROVE", "WELCOME:SMS:1:1", "approver");
        verify(publisher).publishRequired("commonTemplate", "RETIRE", "WELCOME:SMS:1:2", "approver");
    }

    @Test
    void propagatesDurablePublishFailureInsteadOfClaimingSuccess() {
        FakeStore store = new FakeStore();
        CpfCommonCacheRefreshPublisher publisher = mock(CpfCommonCacheRefreshPublisher.class);
        CmnTemplateManagementService service = new CmnTemplateManagementService(store, publisher);
        CmnTemplateDefinition draft = new CmnTemplateDefinition(
                "WELCOME", 1, "SMS", "Hello", Set.of(), false);
        doThrow(new IllegalStateException("outbox unavailable"))
                .when(publisher).publishRequired("commonTemplate", "CREATE_DRAFT", "WELCOME:SMS:1:0", "operator");

        assertThatThrownBy(() -> service.createDraft(draft, "operator", "registration"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outbox unavailable");
    }

    private static final class FakeStore implements CmnTemplateStore {
        private final Map<String, CmnTemplateVersion> values = new LinkedHashMap<>();

        @Override
        public Optional<CmnTemplateDefinition> findActive(String templateCode, String channel) {
            return values.values().stream().filter(v -> v.definition().templateCode().equals(templateCode))
                    .filter(v -> v.definition().channel().equals(channel))
                    .filter(v -> v.definition().active()).map(CmnTemplateVersion::definition).findFirst();
        }

        @Override
        public Optional<CmnTemplateVersion> findVersion(String templateCode, String channel, long version) {
            return Optional.ofNullable(values.get(key(templateCode, channel, version)));
        }

        @Override
        public CmnTemplateVersion createDraft(CmnTemplateDefinition definition, String actor, String reason) {
            CmnTemplateVersion value = new CmnTemplateVersion(definition, CmnTemplateLifecycleStatus.DRAFT, 0);
            values.put(key(definition.templateCode(), definition.channel(), definition.version()), value);
            return value;
        }

        @Override
        public CmnTemplateVersion approve(String templateCode, String channel, long version, long expectedRevision,
                                          String actor, String reason) {
            CmnTemplateVersion current = findVersion(templateCode, channel, version).orElseThrow();
            if (current.revision() != expectedRevision) throw new CmnTemplateConflictException(
                    CmnTemplateConflictException.Type.REVISION_CONFLICT, "revision");
            CmnTemplateDefinition d = current.definition();
            CmnTemplateVersion value = new CmnTemplateVersion(new CmnTemplateDefinition(
                    d.templateCode(), d.version(), d.channel(), d.body(), d.allowedVariables(), true),
                    CmnTemplateLifecycleStatus.APPROVED, current.revision() + 1);
            values.put(key(templateCode, channel, version), value);
            return value;
        }

        @Override
        public CmnTemplateVersion retire(String templateCode, String channel, long version, long expectedRevision,
                                         String actor, String reason) {
            CmnTemplateVersion current = findVersion(templateCode, channel, version).orElseThrow();
            if (current.revision() != expectedRevision) throw new CmnTemplateConflictException(
                    CmnTemplateConflictException.Type.REVISION_CONFLICT, "revision");
            CmnTemplateDefinition d = current.definition();
            CmnTemplateVersion value = new CmnTemplateVersion(new CmnTemplateDefinition(
                    d.templateCode(), d.version(), d.channel(), d.body(), d.allowedVariables(), false),
                    CmnTemplateLifecycleStatus.RETIRED, current.revision() + 1);
            values.put(key(templateCode, channel, version), value);
            return value;
        }

        @Override
        public java.util.List<CmnTemplateAuditEvent> auditHistory(String templateCode, String channel, long version, int limit) {
            return java.util.List.of();
        }

        private String key(String code, String channel, long version) {
            return code + ":" + channel + ":" + version;
        }
    }
}
