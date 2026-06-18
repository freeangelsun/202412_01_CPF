package cpf.adm.opr.service;

import cpf.cmn.mqe.core.CmnMessageConsumer;
import cpf.cmn.mqe.core.CmnMessageEnvelope;
import cpf.cmn.mqe.core.CmnMessagePublisher;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdmDynamicLogLevelBroadcastService {
    public static final String DESTINATION = "cpf.adm.dynamic-log-level";

    private static final Logger log = LoggerFactory.getLogger(AdmDynamicLogLevelBroadcastService.class);

    private final AdmDynamicLogLevelRuleStore ruleStore;
    private final DynamicTransactionLogLevelService runtimeService;
    private final ObjectProvider<CmnMessagePublisher> messagePublisherProvider;
    private final ObjectProvider<CacheRefreshEventPublisher> cacheRefreshEventPublisherProvider;

    public AdmDynamicLogLevelBroadcastService(
            AdmDynamicLogLevelRuleStore ruleStore,
            DynamicTransactionLogLevelService runtimeService,
            @Qualifier("cmnMessageBridgeService") ObjectProvider<CmnMessagePublisher> messagePublisherProvider,
            @Qualifier("cmnMessageBridgeService") ObjectProvider<CmnMessageConsumer> messageConsumerProvider,
            ObjectProvider<CacheRefreshEventPublisher> cacheRefreshEventPublisherProvider) {
        this.ruleStore = ruleStore;
        this.runtimeService = runtimeService;
        this.messagePublisherProvider = messagePublisherProvider;
        this.cacheRefreshEventPublisherProvider = cacheRefreshEventPublisherProvider;
        CmnMessageConsumer consumer = messageConsumerProvider.getIfAvailable();
        if (consumer != null) {
            consumer.subscribe(DESTINATION, this::handleMessage);
        }
    }

    public void publishUpsert(DynamicLogLevelRule rule, String requestUser) {
        Map<String, Object> payload = basePayload("UPSERT", rule.ruleId(), requestUser);
        payload.put("logLevel", rule.logLevel().name());
        payload.put("businessTransactionId", rule.businessTransactionId());
        payload.put("transactionId", rule.transactionId());
        publish(payload);
        publishDatabaseEvent("UPSERT", rule.ruleId(), requestUser);
    }

    public void publishDelete(String ruleId, String requestUser) {
        publish(basePayload("DELETE", ruleId, requestUser));
        publishDatabaseEvent("DELETE", ruleId, requestUser);
    }

    public void syncFromDatabase(String reason) {
        runtimeService.replaceAll(ruleStore.findActiveRules());
        log.debug("Dynamic log-level runtime rules refreshed. reason={}", reason);
    }

    private Map<String, Object> basePayload(String eventType, String ruleId, String requestUser) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", eventType);
        payload.put("ruleId", ruleId);
        payload.put("requestUser", TextUtils.defaultIfBlank(requestUser, "ADM"));
        return payload;
    }

    private void publish(Map<String, Object> payload) {
        CmnMessagePublisher publisher = messagePublisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }
        try {
            publisher.publish(DESTINATION, TextUtils.defaultIfBlank((String) payload.get("ruleId"), "dynamic-log-level"), payload, Map.of(
                    "cpf-event-type", TextUtils.defaultIfBlank((String) payload.get("eventType"), "UNKNOWN"),
                    "cpf-event-domain", "ADM_DYNAMIC_LOG_LEVEL"));
        } catch (RuntimeException ex) {
            log.warn("Failed to publish dynamic log-level message. ruleId={}, message={}", payload.get("ruleId"), ex.getMessage());
        }
    }

    private void publishDatabaseEvent(String eventType, String ruleId, String requestUser) {
        CacheRefreshEventPublisher publisher = cacheRefreshEventPublisherProvider.getIfAvailable();
        if (publisher != null) {
            publisher.publishAfterCommit("dynamicLogLevelRule", eventType, ruleId, requestUser);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(CmnMessageEnvelope envelope) {
        if (!(envelope.payload() instanceof Map<?, ?> payload)) {
            return;
        }
        Object eventType = payload.get("eventType");
        Object ruleId = payload.get("ruleId");
        if (eventType == null || ruleId == null) {
            return;
        }
        syncFromDatabase("message:" + eventType + ":" + ruleId);
    }
}
