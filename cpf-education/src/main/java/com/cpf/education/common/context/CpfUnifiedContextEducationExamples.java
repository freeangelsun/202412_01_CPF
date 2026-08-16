package com.cpf.education.common.context;
import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.starter.async.CpfAsyncContextPropagation;
import com.cpf.starter.async.CpfAsyncForkType;
import com.cpf.messaging.context.CpfMessageContext;
import com.cpf.messaging.context.CpfMessageContextAdapter;
import com.cpf.messaging.context.CpfMessageContextBundle;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpInboundResult;
import com.cpf.web.context.CpfHttpIngressMetadata;
import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import com.cpf.web.context.CpfWebContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 단순화된 CPF Context Golden Path 교육 예제입니다.
 *
 * <p>일반 개발자는 {@link CpfContext}, {@link CpfContexts}, {@link CpfContextSnapshot}만 사용합니다.
 * Header, Message, Batch metadata와 실행 저장 방식은 각 Owner가 소유하며 Core Registry/Factory/Transport
 * 메커니즘을 업무 코드에 노출하지 않습니다.</p>
 */
public final class CpfUnifiedContextEducationExamples {
    private CpfUnifiedContextEducationExamples() { }

    /** 외부 Header는 Web Owner가 검증한 뒤 Core 의미로 승격합니다. */
    public static CpfHttpInboundResult httpIngress(
            CpfHttpInboundContextAdapter adapter,
            Map<String,String> headers,
            CpfHttpIngressMetadata edge,
            CpfContext.CpfIdentityContext authenticated,
            CpfContext.CpfTenantContext tenant,
            LocalDate businessDate) {
        return adapter.resolve(headers, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, authenticated, tenant, edge,
                "EDU-HTTP-IN", businessDate, Instant.now().plusSeconds(10));
    }

    /** REST outbound에서는 일반 개발자가 ID SPI를 조립하지 않고 현재 의미 Context만 사용합니다.
     * Child execution 생성은 Web/Integration Runtime Owner가 담당합니다. */
    public static Map<String,String> restAtoB(
            CpfHttpOutboundContextAdapter outbound,
            CpfWebContext interaction) {
        return outbound.headers(CpfContexts.requireCurrent(), interaction, null);
    }

    /** Async는 Snapshot capture와 lexical bind를 Foundation이 수행합니다. */
    public static String asyncExecutor(CpfAsyncContextPropagation propagation, Executor executor) {
        AtomicReference<String> seen=new AtomicReference<>();
        executor.execute(propagation.wrap(() -> seen.set(CpfContexts.transactionId()), CpfAsyncForkType.EXECUTOR));
        return seen.get();
    }

    /** Message metadata는 Messaging Owner bundle로 유지합니다. */
    public static CpfMessageContextBundle messageConsume(
            CpfMessageContextAdapter adapter, Map<String,String> headers, CpfMessageContext delivery) {
        return adapter.extract(headers, delivery, "EDU-MESSAGE", Instant.now().plusSeconds(30));
    }

    /** Batch metadata는 Core Snapshot의 generic component가 아니라 Batch Owner bundle로 유지합니다. */
    public static CpfBatchContextBundle batchBundle(
            CpfContextSnapshot parent, String jobExecutionId, String checkpointId, long fencingToken) {
        CpfBatchContext batch=new CpfBatchContext("eduJob", "EDU Job", 1, "JI-1", jobExecutionId, jobExecutionId,
                "eduStep", "SE-1", "SCH-1", "TRG-1", CpfBatchLaunchMode.MANUAL,
                parent.transaction().businessDate(), 0, 1, "P-0", null, null, null,
                "worker-1", "edu", null, checkpointId, "RUNNING", null, null, fencingToken, Instant.now());
        return new CpfBatchContextBundle(parent, batch);
    }

    /** Restart는 Core correlation과 Batch recovery metadata를 각각 명시적으로 유지합니다. */
    public static CpfBatchContext batchRestart(
            CpfBatchContext previous, String newJobExecutionId, String recoveryId, long newFencingToken) {
        return previous.withJobExecution(previous.jobInstanceId(), newJobExecutionId,
                previous.restartCount()+1, previous.attempt()+1,
                previous.originalJobExecutionId(), recoveryId, newFencingToken);
    }

    /** transactionId와 traceparent는 서로 다른 식별자입니다. */
    public record TransactionAndTrace(String transactionId, String traceparent) { }
    public static TransactionAndTrace transactionVsTrace(String traceparent) {
        return new TransactionAndTrace(CpfContexts.transactionId(), traceparent);
    }

    /** 일반 업무 코드는 Context의 멱등성 의미를 facade를 통해 읽으며 ID SPI를 직접 조립하지 않습니다. */
    public static String idempotencyKey() {
        CpfContext context=CpfContexts.requireCurrent();
        if(context.operation()==null || context.operation().idempotencyKey()==null) {
            throw new IllegalStateException("education requires idempotency");
        }
        return context.idempotencyKey();
    }

    /** 외부 spoofing header는 인증된 identity/tenant로 승격하지 않습니다. */
    public static boolean spoofRejected(
            CpfHttpInboundContextAdapter adapter, Map<String,String> malicious,
            CpfHttpIngressMetadata edge, LocalDate date) {
        CpfHttpInboundResult result=adapter.resolve(malicious, CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,
                null, null, edge, "EDU-SPOOF", date, Instant.now().plusSeconds(5));
        return result.snapshot().identity()==null && result.snapshot().tenant()==null;
    }

    /** Managed Context가 없으면 일반 개발자 facade는 fail-fast 합니다. */
    public static boolean noContextUnit() {
        try { CpfContexts.requireSnapshot(); return false; }
        catch (IllegalStateException expected) { return true; }
    }
}
