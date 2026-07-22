package com.cpf.reference.messaging.controller;

import com.cpf.common.utils.DateTimeUtils;
import com.cpf.common.utils.IdUtils;
import com.cpf.core.common.broker.CpfBrokerBridgeMessage;
import com.cpf.core.common.broker.CpfBrokerBridgePort;
import com.cpf.core.common.broker.CpfBrokerBridgeResult;
import com.cpf.core.common.execution.CpfOnlineTransaction;
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

@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 05. Messaging", description = "Kafka, RabbitMQ, 인메모리 메시지 어댑터 교육 샘플")
public class ReferenceMessagingEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final CpfBrokerBridgePort brokerBridgePort;
    private final List<CpfBrokerBridgeMessage> consumedMessages = new CopyOnWriteArrayList<>();

    public ReferenceMessagingEducationController(CpfBrokerBridgePort brokerBridgePort) {
        this.brokerBridgePort = brokerBridgePort;
        this.brokerBridgePort.subscribe("com.cpf.reference.event", consumedMessages::add);
    }

    @PostMapping("/messaging/publish")
    @CpfOnlineTransaction(id = "OREFAA0025", name = "REFMessagePublish")
    @Operation(operationId = "refMessagingEducationPublishMessage", summary = "메시지 발행 샘플", description = "CMN 메시지 추상화 계층을 통해 메시지 봉투를 발행합니다.")
    public ResponseEntity<Map<String, Object>> publishMessage(
            @RequestParam(defaultValue = "com.cpf.reference.event") String destination,
            @RequestParam(defaultValue = "REF Reference-SAMPLE") String key,
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> resolvedPayload = payload == null || payload.isEmpty()
                ? Map.of("sampleId", IdUtils.temporaryId("REF"), "message", "REF 교육 메시지 샘플", "createdAt", DateTimeUtils.nowDateTimeMillis())
                : payload;

        CpfBrokerBridgeResult publishResult = brokerBridgePort.publish(destination, key, resolvedPayload, Map.of(
                "X-Edu-Sample", "Y",
                "X-Edu-Source", "REF"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("publishResult", publishResult);
        response.put("recentMessages", brokerBridgePort.findRecent(destination, 10));
        response.put("consumedMessages", consumedMessages);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messaging/recent")
    @CpfOnlineTransaction(id = "OREFAA0051", name = "REFMessageRecent")
    @Operation(operationId = "refMessagingEducationFindRecentMessages", summary = "최근 메시지 조회 샘플", description = "현재 활성화된 메시지 어댑터의 최근 메시지를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findRecentMessages(
            @RequestParam(defaultValue = "com.cpf.reference.event") String destination) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recentMessages", brokerBridgePort.findRecent(destination, 50));
        response.put("consumedMessages", consumedMessages);
        response.put("guide", "운영 adapter는 CPF broker bridge port와 Kafka 또는 RabbitMQ listener를 연결합니다.");
        return ResponseEntity.ok(response);
    }
}
