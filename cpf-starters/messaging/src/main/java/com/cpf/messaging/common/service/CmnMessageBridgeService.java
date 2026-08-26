package com.cpf.messaging.common.service;


import com.cpf.messaging.common.config.CmnMessagingProperties;
import com.cpf.messaging.common.api.CmnMessageConsumer;
import com.cpf.messaging.common.api.CmnMessageEnvelope;
import com.cpf.messaging.common.api.CmnMessageHandler;
import com.cpf.messaging.common.api.CmnMessagePublishResult;
import com.cpf.messaging.common.api.CmnMessagePublisher;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.core.api.context.CpfContexts;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 프로젝트 공통 메시지 API를 CPF broker bridge에 연결하는 호환 facade입니다.
 *
 * <p>CPF-OWNERSHIP:CMN_PROJECT_HELPER</p>
 * <p>CMN은 프로젝트별 destination 기본값과 기존 DTO 변환만 담당합니다. broker SDK 호출,
 * 거래 header 전파, 발행 adapter와 최근 메시지 저장은 CPF가 소유합니다.</p>
 */
@Service
public class CmnMessageBridgeService extends com.cpf.foundation.api.CpfBaseService implements CmnMessagePublisher, CmnMessageConsumer {
    private final CmnMessagingProperties properties;
    private final CpfBrokerBridgePort brokerBridgePort;

    public CmnMessageBridgeService(
            CmnMessagingProperties properties,
            CpfBrokerBridgePort brokerBridgePort) {
        this.properties = properties;
        this.brokerBridgePort = brokerBridgePort;
    }

    @Override
    public CmnMessagePublishResult publish(String key, Object payload) {
        return publish(properties.getDefaultDestination(), key, payload, Map.of());
    }

    @Override
    public CmnMessagePublishResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> headers) {
        String resolvedDestination = CpfStrings.defaultIfBlank(destination, properties.getDefaultDestination());
        String resolvedKey = CpfStrings.defaultIfBlank(key, CpfContexts.transactionId());
        CpfBrokerBridgeResult result = brokerBridgePort.publish(
                resolvedDestination,
                resolvedKey,
                payload,
                headers);
        return new CmnMessagePublishResult(
                result.accepted(),
                result.transport(),
                result.destination(),
                result.messageId(),
                result.transactionId(),
                result.detail());
    }

    @Override
    public void subscribe(String destination, CmnMessageHandler handler) {
        String resolvedDestination = CpfStrings.defaultIfBlank(destination, properties.getDefaultDestination());
        brokerBridgePort.subscribe(resolvedDestination, message -> handler.handle(toCmnEnvelope(message)));
    }

    @Override
    public List<CmnMessageEnvelope> findRecentMessages(String destination, int limit) {
        int resolvedLimit = Math.min(
                limit <= 0 ? 50 : limit,
                Math.max(1, properties.getRecentMessageLimit()));
        return brokerBridgePort.findRecent(destination, resolvedLimit).stream()
                .map(this::toCmnEnvelope)
                .toList();
    }

    private CmnMessageEnvelope toCmnEnvelope(CpfBrokerBridgeMessage message) {
        return new CmnMessageEnvelope(
                message.transport(),
                message.destination(),
                message.key(),
                message.payload(),
                message.headers(),
                message.createdAt().toString());
    }
}
