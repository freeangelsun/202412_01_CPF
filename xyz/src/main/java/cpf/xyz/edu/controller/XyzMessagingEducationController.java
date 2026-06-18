package cpf.xyz.edu.controller;

import cpf.cmn.mqe.core.CmnMessageConsumer;
import cpf.cmn.mqe.core.CmnMessageEnvelope;
import cpf.cmn.mqe.core.CmnMessagePublishResult;
import cpf.cmn.mqe.core.CmnMessagePublisher;
import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 05. Messaging", description = "Kafka, RabbitMQ, 인메모리 메시지 어댑터 교육 샘플")
public class XyzMessagingEducationController {
    private final CmnMessagePublisher messagePublisher;
    private final CmnMessageConsumer messageConsumer;
    private final List<CmnMessageEnvelope> consumedMessages = new CopyOnWriteArrayList<>();

    public XyzMessagingEducationController(
            @Qualifier("cmnMessageBridgeService") CmnMessagePublisher messagePublisher,
            @Qualifier("cmnMessageBridgeService") CmnMessageConsumer messageConsumer) {
        this.messagePublisher = messagePublisher;
        this.messageConsumer = messageConsumer;
        this.messageConsumer.subscribe("cpf.xyz.edu.event", consumedMessages::add);
    }

    @PostMapping("/messaging/publish")
    @CpfTransaction(id = "XYZ09EDU0010", name = "XYZMessagePublish")
    @Operation(summary = "메시지 발행 샘플", description = "CMN 메시지 추상화 계층을 통해 메시지 봉투를 발행합니다.")
    public ResponseEntity<Map<String, Object>> publishMessage(
            @RequestParam(defaultValue = "cpf.xyz.edu.event") String destination,
            @RequestParam(defaultValue = "XYZ-EDU-SAMPLE") String key,
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> resolvedPayload = payload == null || payload.isEmpty()
                ? Map.of("sampleId", IdUtils.temporaryId("XYZ"), "message", "XYZ 교육 메시지 샘플", "createdAt", DateTimeUtils.nowDateTimeMillis())
                : payload;

        CmnMessagePublishResult publishResult = messagePublisher.publish(destination, key, resolvedPayload, Map.of(
                "X-Edu-Sample", "Y",
                "X-Edu-Source", "XYZ"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("publishResult", publishResult);
        response.put("recentMessages", messageConsumer.findRecentMessages(destination, 10));
        response.put("consumedMessages", consumedMessages);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messaging/recent")
    @CpfTransaction(id = "XYZ09EDU0011", name = "XYZMessageRecent")
    @Operation(summary = "최근 메시지 조회 샘플", description = "현재 활성화된 메시지 어댑터의 최근 메시지를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findRecentMessages(
            @RequestParam(defaultValue = "cpf.xyz.edu.event") String destination) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recentMessages", messageConsumer.findRecentMessages(destination, 50));
        response.put("consumedMessages", consumedMessages);
        response.put("guide", "운영 어댑터는 Kafka 또는 RabbitMQ listener를 통해 CmnMessageEnvelope를 소비해야 합니다.");
        return ResponseEntity.ok(response);
    }
}
