package com.cpf.integration.realtime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
public final class CpfRealtimeController {
    private final CpfRealtimeBroker broker;
    private final CpfRealtimeAuthorization authorization;
    private final CpfRealtimeProperties properties;

    public CpfRealtimeController(CpfRealtimeBroker broker, CpfRealtimeAuthorization authorization, CpfRealtimeProperties properties) {
        this.broker = broker;
        this.authorization = authorization;
        this.properties = properties;
    }

    @GetMapping(value = "${cpf.integration.realtime.stream-path:/cpf/realtime/stream}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Principal principal,
                             @RequestParam String tenantId,
                             @RequestParam String channel,
                             @RequestParam String topic,
                             @RequestParam(defaultValue = "") String subjectId,
                             @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId) {
        authorize(principal, tenantId, channel, topic, subjectId);
        String cursor = lastEventId == null ? "" : lastEventId.trim();
        SseEmitter emitter = new SseEmitter(properties.getEmitterTimeout().toMillis());
        CpfRealtimeBroker.Filter filter = new CpfRealtimeBroker.Filter(tenantId, channel, topic, subjectId);
        CpfRealtimeBroker.Subscription subscription = broker.subscribe(filter, cursor, delivery -> send(emitter, delivery));
        emitter.onCompletion(subscription::close);
        emitter.onTimeout(subscription::close);
        emitter.onError(error -> subscription.close());
        return emitter;
    }

    @GetMapping("${cpf.integration.realtime.poll-path:/cpf/realtime/events}")
    public List<CpfRealtimeEvent> poll(Principal principal,
                                       @RequestParam String tenantId,
                                       @RequestParam String channel,
                                       @RequestParam String topic,
                                       @RequestParam(defaultValue = "") String subjectId,
                                       @RequestParam(defaultValue = "") String afterEventId,
                                       @RequestParam(defaultValue = "100") int limit) {
        authorize(principal, tenantId, channel, topic, subjectId);
        return broker.poll(new CpfRealtimeBroker.Filter(tenantId, channel, topic, subjectId), afterEventId, limit);
    }

    private void authorize(Principal principal, String tenantId, String channel, String topic, String subjectId) {
        if (!authorization.canSubscribe(principal, tenantId, channel, topic, subjectId)) throw new RealtimeForbiddenException();
    }

    private static void send(SseEmitter emitter, CpfRealtimeBroker.Delivery delivery) {
        try {
            if (delivery instanceof CpfRealtimeBroker.Delivery.Event item) {
                CpfRealtimeEvent event = item.event();
                emitter.send(SseEmitter.event().id(event.eventId()).name(event.topic()).data(event));
            } else if (delivery instanceof CpfRealtimeBroker.Delivery.Heartbeat heartbeat) {
                emitter.send(SseEmitter.event().comment("heartbeat " + heartbeat.at()));
            }
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
            throw new RealtimeDeliveryException(e);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    private static final class RealtimeForbiddenException extends RuntimeException { private static final long serialVersionUID = 1L; }
    private static final class RealtimeDeliveryException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private RealtimeDeliveryException(Throwable cause) { super(cause); }
    }
}
