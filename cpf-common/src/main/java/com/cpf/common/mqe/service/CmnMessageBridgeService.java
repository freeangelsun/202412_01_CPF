package com.cpf.common.mqe.service;

import com.cpf.common.mqe.config.CmnMessagingProperties;
import com.cpf.common.mqe.core.CmnMessageConsumer;
import com.cpf.common.mqe.core.CmnMessageEnvelope;
import com.cpf.common.mqe.core.CmnMessageHandler;
import com.cpf.common.mqe.core.CmnMessagePublishResult;
import com.cpf.common.mqe.core.CmnMessagePublisher;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.broker.CpfBrokerBridgeMessage;
import com.cpf.core.common.broker.CpfBrokerBridgePort;
import com.cpf.core.common.broker.CpfBrokerBridgeResult;
import com.cpf.core.common.logging.TransactionContext;
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
public class CmnMessageBridgeService extends com.cpf.common.common.base.CmnBaseService implements CmnMessagePublisher, CmnMessageConsumer {
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
        String resolvedDestination = TextUtils.defaultIfBlank(destination, properties.getDefaultDestination());
        String resolvedKey = TextUtils.defaultIfBlank(key, TransactionContext.getOrCreateTransactionId());
        CpfBrokerBridgeResult result = brokerBridgePort.publish(
                resolvedDestination,
                resolvedKey,
                payload,
                headers);
        return new CmnMessagePublishResult(
                result.success(),
                result.broker(),
                result.destination(),
                result.key(),
                result.transactionId(),
                result.detail());
    }

    @Override
    public void subscribe(String destination, CmnMessageHandler handler) {
        String resolvedDestination = TextUtils.defaultIfBlank(destination, properties.getDefaultDestination());
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
                message.broker(),
                message.destination(),
                message.key(),
                message.payload(),
                message.headers(),
                message.createdAt().toString());
    }
}
