package com.cpf.integration.graphql;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.starter.async.CpfReactorContextBridge;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

/** GraphQL auth/tenant/field/rate/size/timeout guard와 CPF Context 전파를 수행한다. */
public final class CpfGraphqlGuardInterceptor implements WebGraphQlInterceptor {
    private final CpfGraphqlProperties properties;
    private final CpfGraphqlRateLimiter rateLimiter;
    private final CpfGraphqlAuthorizationPolicy authorization;
    private final CpfGraphqlAuditSink audit;
    private final CpfGraphqlTelemetry telemetry;

    public CpfGraphqlGuardInterceptor(CpfGraphqlProperties properties, CpfGraphqlRateLimiter rateLimiter,
            CpfGraphqlAuthorizationPolicy authorization, CpfGraphqlAuditSink audit, CpfGraphqlTelemetry telemetry) {
        this.properties=properties; this.rateLimiter=rateLimiter; this.authorization=authorization; this.audit=audit; this.telemetry=telemetry;
        properties.validate();
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        long started=System.nanoTime();
        String document=request.getDocument();
        if(document!=null&&document.length()>properties.getMaxDocumentLength())return Mono.error(new IllegalArgumentException("GraphQL document too large"));
        int approximateBytes=(document==null?0:document.getBytes(StandardCharsets.UTF_8).length)+String.valueOf(request.getVariables()).getBytes(StandardCharsets.UTF_8).length;
        if(approximateBytes>properties.getMaxRequestBytes())return Mono.error(new IllegalArgumentException("GraphQL request too large"));
        if(!properties.isIntrospection()&&document!=null&&document.toLowerCase(Locale.ROOT).contains("__schema"))return Mono.error(new IllegalArgumentException("GraphQL introspection disabled"));
        CpfContextSnapshot parent=CpfContexts.snapshot();
        if(parent==null)return properties.isRequireAuthenticated()?Mono.error(new SecurityException("CPF context required for GraphQL")):chain.next(request);
        CpfContext context=parent.context(); String subject=context.subjectId(); String tenant=context.tenantId();
        if(properties.isRequireAuthenticated()&&(subject==null||subject.isBlank()))return Mono.error(new SecurityException("authenticated subject required"));
        if(properties.isRequireTenant()&&(tenant==null||tenant.isBlank()))return Mono.error(new SecurityException("tenant required"));
        String digest=sha(document);
        if(!authorization.authorizeOperation(context,request.getOperationName(),digest))return Mono.error(new SecurityException("GraphQL operation forbidden"));
        for(String field:CpfGraphqlDocumentFields.paths(document)) if(!authorization.authorizeField(context,field)) return Mono.error(new SecurityException("GraphQL field forbidden"));
        String key=(tenant==null?"_":tenant)+":"+(subject==null?"anonymous":subject);
        if(!rateLimiter.allow(key,properties.getMaxRequestsPerSecond()))return Mono.error(new IllegalStateException("GraphQL rate limit exceeded"));
        CpfGraphqlContext operationContext=new CpfGraphqlContext(request.getId(),request.getOperationName(),"GRAPHQL",digest,parent.context().execution().attempt());
        Mono<WebGraphQlResponse> deferred=Mono.defer(()->chain.next(request));
        return CpfReactorContextBridge.bindSnapshot(deferred,parent)
                .timeout(properties.getTimeout())
                .doOnSuccess(response->record(operationContext,context,"SUCCESS",started))
                .doOnError(error->record(operationContext,context,"FAIL",started));
    }

    private void record(CpfGraphqlContext operationContext,CpfContext context,String result,long started){
        long elapsed=Math.max(0L,(System.nanoTime()-started)/1_000_000L);
        telemetry.record(operationContext.operationName(),result,elapsed);
        audit.record(new CpfGraphqlAuditSink.Event(operationContext.operationId(),operationContext.operationName(),context.tenantId(),context.subjectId(),context.transactionId(),operationContext.documentSha256(),result,elapsed,Instant.now()));
    }
    private static String sha(String value){if(value==null)return null;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception failure){throw new IllegalStateException("SHA-256 unavailable",failure);}}
}
