package com.cpf.common.template;

import com.cpf.foundation.api.CpfBaseService;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Product administration service with audit, optimistic locking and durable multi-instance refresh. */
@Service
@ConditionalOnBean(CmnTemplateStore.class)
public final class CmnTemplateManagementService extends CpfBaseService {
    private static final String CACHE_NAME = "commonTemplate";
    private final CmnTemplateStore store;
    private final CpfCommonCacheRefreshPublisher refreshPublisher;

    public CmnTemplateManagementService(
            CmnTemplateStore store,
            CpfCommonCacheRefreshPublisher refreshPublisher) {
        this.store = Objects.requireNonNull(store, "store");
        this.refreshPublisher = Objects.requireNonNull(refreshPublisher, "refreshPublisher");
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager", readOnly = true)
    public List<CmnTemplateAuditEntry> auditHistory(
            String templateCode, String channel, long version, int limit) {
        return store.auditHistory(templateCode, channel, version, limit);
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public CmnTemplateVersion createDraft(CmnTemplateDefinition definition, String actor, String reason) {
        CmnTemplateVersion created = store.createDraft(definition, actor, reason);
        publish("CREATE_DRAFT", created, actor);
        return created;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public CmnTemplateVersion approve(
            String templateCode,
            String channel,
            long version,
            long expectedRevision,
            String actor,
            String reason) {
        CmnTemplateVersion approved = store.approve(
                templateCode, channel, version, expectedRevision, actor, reason);
        publish("APPROVE", approved, actor);
        return approved;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public CmnTemplateVersion retire(
            String templateCode,
            String channel,
            long version,
            long expectedRevision,
            String actor,
            String reason) {
        CmnTemplateVersion retired = store.retire(
                templateCode, channel, version, expectedRevision, actor, reason);
        publish("RETIRE", retired, actor);
        return retired;
    }

    private void publish(String operation, CmnTemplateVersion version, String actor) {
        CmnTemplateDefinition definition = version.definition();
        String eventKey = definition.templateCode() + ":" + definition.channel() + ":" + definition.version()
                + ":" + version.revision();
        refreshPublisher.publishRequired(CACHE_NAME, operation, eventKey, actor);
    }
}
