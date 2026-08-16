package com.cpf.admin.opr.service;

import com.cpf.messaging.common.api.CmnMessageConsumer;
import com.cpf.messaging.common.api.CmnMessageEnvelope;
import com.cpf.messaging.common.api.CmnMessagePublisher;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshPublisher;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.cpf.platform.operations.observability.api.logging.CpfDynamicLogLevelOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import com.cpf.foundation.annotation.CpfService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM에서 동적 로그 레벨 정책 변경을 Runtime과 다중 인스턴스에 전파하는 운영 서비스입니다.
 *
 * <p>정책 저장소를 기준으로 로컬 Runtime을 동기화하고, 메시징 및 DB 기반 refresh event를 통해
 * 다른 인스턴스에도 변경을 전달합니다. 전파 채널 장애는 원 업무 트랜잭션을 오염시키지 않도록
 * 경고 로그로 격리하며, 최종 상태는 저장소 재동기화로 복구할 수 있습니다.</p>
 */
@CpfService
public class AdmDynamicLogLevelBroadcastService extends com.cpf.admin.common.base.AdmBaseService {
    public static final String DESTINATION = "com.cpf.admin.dynamic-log-level";

    private static final Logger log = LoggerFactory.getLogger(AdmDynamicLogLevelBroadcastService.class);

    private final AdmDynamicLogLevelRuleStore ruleStore;
    private final CpfDynamicLogLevelOperations runtimeService;
    private final ObjectProvider<CmnMessagePublisher> messagePublisherProvider;
    private final ObjectProvider<CpfCommonCacheRefreshPublisher> cacheRefreshEventPublisherProvider;

    public AdmDynamicLogLevelBroadcastService(
            AdmDynamicLogLevelRuleStore ruleStore,
            CpfDynamicLogLevelOperations runtimeService,
            @Qualifier("cmnMessageBridgeService") ObjectProvider<CmnMessagePublisher> messagePublisherProvider,
            @Qualifier("cmnMessageBridgeService") ObjectProvider<CmnMessageConsumer> messageConsumerProvider,
            ObjectProvider<CpfCommonCacheRefreshPublisher> cacheRefreshEventPublisherProvider) {
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
        payload.put("requestUser", CpfStrings.defaultIfBlank(requestUser, "ADM"));
        return payload;
    }

    private void publish(Map<String, Object> payload) {
        CmnMessagePublisher publisher = messagePublisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }
        try {
            publisher.publish(DESTINATION, CpfStrings.defaultIfBlank((String) payload.get("ruleId"), "dynamic-log-level"), payload, Map.of(
                    "cpf-event-type", CpfStrings.defaultIfBlank((String) payload.get("eventType"), "UNKNOWN"),
                    "cpf-event-domain", "ADM_DYNAMIC_LOG_LEVEL"));
        } catch (RuntimeException ex) {
            log.warn("Failed to publish dynamic log-level message. ruleId={}, message={}", payload.get("ruleId"), ex.getMessage());
        }
    }

    private void publishDatabaseEvent(String eventType, String ruleId, String requestUser) {
        CpfCommonCacheRefreshPublisher publisher = cacheRefreshEventPublisherProvider.getIfAvailable();
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
