package com.cpf.admin.opr.gateway;

import com.cpf.gateway.api.CpfGatewayRegistryPort;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Gateway 운영 Event를 Last-Event-ID 기반으로 재개 가능한 SSE로 전달합니다. */
@RestController
@RequestMapping("/adm/api/gateway-registry/operations")
public final class AdmGatewayOperationsStreamController {
    private final ObjectProvider<CpfGatewayRegistryPort> portProvider;
    private final ExecutorService executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
    private final long timeoutMs;
    private final long pollMs;

    public AdmGatewayOperationsStreamController(
            ObjectProvider<CpfGatewayRegistryPort> portProvider,
            @Value("${cpf.admin.gateway.operations.sse-timeout-ms:300000}") long timeoutMs,
            @Value("${cpf.admin.gateway.operations.sse-poll-ms:1000}") long pollMs) {
        this.portProvider=portProvider;
        this.timeoutMs=Math.max(30_000,Math.min(timeoutMs,1_800_000));
        this.pollMs=Math.max(250,Math.min(pollMs,10_000));
    }

    @GetMapping(value="/stream",produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(operationId="admGatewayOperationsStream",summary="Gateway 운영 Event SSE Stream")
    public SseEmitter stream(
            @RequestHeader(name="Last-Event-ID",required=false) String lastEventId,
            @RequestParam(required=false) String afterEventId,
            HttpServletRequest request) {
        Object operator=request.getAttribute("adm.operatorId");
        if(!(operator instanceof String value)||value.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM 운영자가 필요합니다.");
        CpfGatewayRegistryPort port=portProvider.getIfAvailable();
        if(port==null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Gateway Control Plane이 설치되지 않았습니다.");
        String initial=clean(afterEventId).isBlank()?clean(lastEventId):clean(afterEventId);
        SseEmitter emitter=new SseEmitter(timeoutMs);
        AtomicBoolean closed=new AtomicBoolean(false);
        emitter.onCompletion(()->closed.set(true));
        emitter.onTimeout(()->closed.set(true));
        emitter.onError(error->closed.set(true));
        executor.submit(()->pump(port,emitter,closed,initial));
        return emitter;
    }

    private void pump(CpfGatewayRegistryPort port,SseEmitter emitter,AtomicBoolean closed,String initial) {
        String cursor=initial;
        long heartbeatAt=0;
        long started=System.nanoTime();
        try {
            while(!closed.get()&&Duration.ofNanos(System.nanoTime()-started).toMillis()<timeoutMs) {
                List<CpfGatewayRegistryPort.OperationsEvent> events=port.operationsEvents(cursor,100);
                for(CpfGatewayRegistryPort.OperationsEvent event:events) {
                    emitter.send(SseEmitter.event().id(event.eventId()).name(event.eventType()).data(event));
                    cursor=event.eventId();
                }
                long now=System.currentTimeMillis();
                if(now-heartbeatAt>=15_000) {
                    emitter.send(SseEmitter.event().name("gateway-heartbeat").data(port.operationsSnapshot()));
                    heartbeatAt=now;
                }
                Thread.sleep(pollMs);
            }
            if(!closed.get()) emitter.complete();
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            emitter.completeWithError(ex);
        } catch(IOException|RuntimeException ex) {
            if(!closed.get()) emitter.completeWithError(ex);
        }
    }

    @PreDestroy
    void close() { executor.close(); }

    private static String clean(String value){return value==null?"":value.trim();}
}
