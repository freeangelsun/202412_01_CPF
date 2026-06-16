package cpf.xyz.edu.controller;

import cpf.cmn.mqe.core.CmnMessageConsumer;
import cpf.cmn.mqe.core.CmnMessageEnvelope;
import cpf.cmn.mqe.core.CmnMessagePublishResult;
import cpf.cmn.mqe.core.CmnMessagePublisher;
import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.pfw.common.logging.FpsTransaction;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 05. Messaging", description = "Kafka, RabbitMQ, and in-memory message adapter samples")
public class XyzMessagingEducationController {
    private final CmnMessagePublisher messagePublisher;
    private final CmnMessageConsumer messageConsumer;
    private final List<CmnMessageEnvelope> consumedMessages = new CopyOnWriteArrayList<>();

    public XyzMessagingEducationController(CmnMessagePublisher messagePublisher, CmnMessageConsumer messageConsumer) {
        this.messagePublisher = messagePublisher;
        this.messageConsumer = messageConsumer;
        this.messageConsumer.subscribe("cpf.xyz.edu.event", consumedMessages::add);
    }

    @PostMapping("/messaging/publish")
    @FpsTransaction(id = "XYZ09EDU0010", name = "XYZMessagePublish")
    @Operation(summary = "Message publish sample", description = "Publishes an envelope through the CMN message abstraction.")
    public ResponseEntity<Map<String, Object>> publishMessage(
            @RequestParam(defaultValue = "cpf.xyz.edu.event") String destination,
            @RequestParam(defaultValue = "XYZ-EDU-SAMPLE") String key,
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> resolvedPayload = payload == null || payload.isEmpty()
                ? Map.of("sampleId", IdUtils.temporaryId("XYZ"), "message", "XYZ education message sample", "createdAt", DateTimeUtils.nowDateTimeMillis())
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
    @FpsTransaction(id = "XYZ09EDU0011", name = "XYZMessageRecent")
    @Operation(summary = "Recent message sample", description = "Returns recent messages from the active message adapter.")
    public ResponseEntity<Map<String, Object>> findRecentMessages(
            @RequestParam(defaultValue = "cpf.xyz.edu.event") String destination) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recentMessages", messageConsumer.findRecentMessages(destination, 50));
        response.put("consumedMessages", consumedMessages);
        response.put("guide", "Production adapters should consume CmnMessageEnvelope through Kafka or RabbitMQ listeners.");
        return ResponseEntity.ok(response);
    }
}
