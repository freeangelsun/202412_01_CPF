package com.cpf.education.messaging.controller;
import com.cpf.foundation.util.CpfTimes;
import com.cpf.foundation.id.CpfIds;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.education.messaging.EducationBrokerPublishEducationSample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CPF 메시징 추상화의 발행·구독·최근 메시지 조회 흐름을 보여주는 EDU 교육 Controller입니다.
 *
 * <p>업무 코드는 특정 Broker 구현에 결합하지 않고 Port를 사용하며,
 * 실제 운영에서는 선택된 Kafka/RabbitMQ 등의 Adapter가 같은 계약을 구현합니다.</p>
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 05. Messaging", description = "Kafka, RabbitMQ, 인메모리 메시지 어댑터 교육 샘플")
public class EducationMessagingEducationController extends com.cpf.education.base.EducationBaseController {
    private final CpfBrokerBridgePort brokerBridgePort;
    private final EducationBrokerPublishEducationSample brokerPublishSample;
    private final List<CpfBrokerBridgeMessage> consumedMessages = new CopyOnWriteArrayList<>();
    private final String subscriptionStatus;

    /** EducationMessagingEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationMessagingEducationController(
            CpfBrokerBridgePort brokerBridgePort,
            CpfBrokerClient brokerClient) {
        this.brokerBridgePort = brokerBridgePort;
        this.brokerPublishSample = new EducationBrokerPublishEducationSample(brokerClient);
        String status;
        try {
            this.brokerBridgePort.subscribe("com.cpf.education.event", consumedMessages::add);
            status = "SUBSCRIBED";
        } catch (UnsupportedOperationException unavailable) {
            status = "PROVIDER_LISTENER_NOT_CONFIGURED";
        }
        this.subscriptionStatus = status;
    }

    @PostMapping("/messaging/publish")
    @CpfOnlineTransaction(id = "OEDUAA0025", name = "EDUMessagePublish", ownerDomain="EDU")
    @Operation(operationId = "refMessagingEducationPublishMessage", summary = "메시지 발행 샘플", description = "CMN 메시지 추상화 계층을 통해 메시지 봉투를 발행합니다.")
    /** publishMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> publishMessage(
            @RequestParam(defaultValue = "com.cpf.education.event") String destination,
            @RequestParam(defaultValue = "EDU Education-SAMPLE") String key,
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> resolvedPayload = payload == null || payload.isEmpty()
                ? Map.of("sampleId", CpfIds.temporaryId("EDU"), "message", "EDU 교육 메시지 샘플", "createdAt", CpfTimes.nowDateTimeMillis())
                : payload;

        CpfBrokerBridgeResult publishResult = brokerBridgePort.publish(destination, key, resolvedPayload, Map.of(
                "X-Edu-Sample", "Y",
                "X-Edu-Source", "EDU"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("publishResult", publishResult);
        response.put("recentMessages", brokerBridgePort.findRecent(destination, 10));
        response.put("consumedMessages", consumedMessages);
        response.put("subscriptionStatus", subscriptionStatus);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messaging/enqueue")
    @CpfOnlineTransaction(id = "OEDUAA0052", name = "EDUMessageEnqueue", ownerDomain="EDU")
    @Operation(
            operationId = "refMessagingEducationEnqueueMessage",
            summary = "신뢰성 메시지 Enqueue 샘플",
            description = "CpfBrokerClient를 통해 업무 트랜잭션 Outbox에 저장하고 ACCEPTED를 반환합니다. 실제 Provider 결과는 Worker와 운영 조회에서 확인합니다.")
    /** enqueueMessage 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CpfBrokerPublishResult> enqueueMessage(
            @RequestParam String transactionId,
            @RequestParam String idempotencyKey) {
        return ResponseEntity.ok(brokerPublishSample.publish(transactionId, idempotencyKey));
    }

    @GetMapping("/messaging/recent")
    @CpfOnlineTransaction(id = "OEDUAA0051", name = "EDUMessageRecent", ownerDomain="EDU")
    @Operation(operationId = "refMessagingEducationFindRecentMessages", summary = "최근 메시지 조회 샘플", description = "현재 활성화된 메시지 어댑터의 최근 메시지를 조회합니다.")
    /** findRecentMessages 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> findRecentMessages(
            @RequestParam(defaultValue = "com.cpf.education.event") String destination) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recentMessages", brokerBridgePort.findRecent(destination, 50));
        response.put("consumedMessages", consumedMessages);
        response.put("subscriptionStatus", subscriptionStatus);
        response.put("guide", "운영 adapter는 CPF broker bridge port와 Kafka 또는 RabbitMQ listener를 연결합니다.");
        return ResponseEntity.ok(response);
    }
}
