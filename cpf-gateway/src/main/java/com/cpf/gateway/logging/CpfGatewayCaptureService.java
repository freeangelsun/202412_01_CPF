package com.cpf.gateway.logging;

import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.core.api.logging.policy.CpfLogCaptureGuard;
import com.cpf.core.api.logging.policy.CpfLogPolicyResolver;
import com.cpf.core.api.logging.policy.CpfPayloadProtectionPort;
import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.transport.CpfGatewayProxyResponse;
import com.cpf.gateway.transport.CpfGatewayReplayableBody;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** Gateway 요청/응답 Capture 정책을 저장 직전에 강제하는 Runtime Consumer입니다. */
@Component
public final class CpfGatewayCaptureService {
    private final ObjectProvider<CpfLogPolicyResolver> resolverProvider;
    private final ObjectProvider<CpfPayloadProtectionPort> protectionProvider;
    private final CpfGatewayLedgerPort ledger;
    private final CpfGatewaySafetyEnforcer safety;

    public CpfGatewayCaptureService(ObjectProvider<CpfLogPolicyResolver> resolverProvider,
            ObjectProvider<CpfPayloadProtectionPort> protectionProvider, CpfGatewayLedgerPort ledger) {
        this(resolverProvider, protectionProvider, ledger,
                new CpfGatewaySafetyEnforcer(new CpfGatewaySafetyProperties()));
    }

    @Autowired
    public CpfGatewayCaptureService(ObjectProvider<CpfLogPolicyResolver> resolverProvider,
            ObjectProvider<CpfPayloadProtectionPort> protectionProvider, CpfGatewayLedgerPort ledger,
            CpfGatewaySafetyEnforcer safety) {
        this.resolverProvider=resolverProvider;
        this.protectionProvider=protectionProvider;
        this.ledger=ledger;
        this.safety=safety;
    }

    public LogPolicyDecision resolve(String routeOrTransactionId) {
        CpfLogPolicyResolver resolver=resolverProvider.getIfAvailable();
        LogPolicyDecision decision = resolver==null?LogPolicyDecision.cpfDefault(
                com.cpf.core.api.logging.policy.LogPolicyTargetType.ONLINE_TRANSACTION,routeOrTransactionId)
                :resolver.resolveOnlineTransaction(routeOrTransactionId);
        safety.validateLogPolicy(decision);
        return decision;
    }

    public void captureRequest(String gatewayTransactionId,String rawQuery,HttpHeaders headers,
            CpfGatewayReplayableBody body,LogPolicyDecision policy) {
        safety.validateLogPolicy(policy);
        record(gatewayTransactionId,"QUERY",policy,CpfLogCaptureGuard.query(rawQuery,policy),rawQuery==null?0:rawQuery.length());
        record(gatewayTransactionId,"REQUEST_HEADERS",policy,
                CpfLogCaptureGuard.headers(headers,false,policy),headerBytes(headers));
        if(policy.requestBodyCaptureMode()==LogCaptureMode.NONE) return;
        byte[] preview=body.readUpTo(policy.maxRequestBodyBytes());
        boolean overflow=body.length()>policy.maxRequestBodyBytes();
        String text=new String(preview,0,Math.min(preview.length,policy.maxRequestBodyBytes()),StandardCharsets.UTF_8);
        var captured=CpfLogCaptureGuard.body(text,false,policy,protectionProvider.getIfAvailable());
        if(overflow&&!captured.truncated()) captured=new CpfLogCaptureGuard.CapturedValue(captured.value(),true,captured.metadataOnly());
        record(gatewayTransactionId,"REQUEST_BODY",policy,captured,body.length());
    }

    public CpfGatewayProxyResponse wrapResponse(String gatewayTransactionId,CpfGatewayProxyResponse response,
            LogPolicyDecision policy) {
        safety.validateLogPolicy(policy);
        safety.validateResponse(response.headers());
        record(gatewayTransactionId,"RESPONSE_HEADERS",policy,
                CpfLogCaptureGuard.headers(response.headers(),true,policy),headerBytes(response.headers()));
        if(policy.responseBodyCaptureMode()==LogCaptureMode.NONE) return response;
        return response.mapBody(input -> new CaptureInputStream(input,policy.maxResponseBodyBytes(),
                (bytes,total,truncated)->{
                    String text=new String(bytes,StandardCharsets.UTF_8);
                    var captured=CpfLogCaptureGuard.body(text,true,policy,protectionProvider.getIfAvailable());
                    if(truncated&&!captured.truncated()) captured=new CpfLogCaptureGuard.CapturedValue(captured.value(),true,captured.metadataOnly());
                    record(gatewayTransactionId,"RESPONSE_BODY",policy,captured,total);
                }));
    }

    public void captureError(String gatewayTransactionId,Throwable error,LogPolicyDecision policy) {
        StringBuilder stack=new StringBuilder(error.toString());
        for(StackTraceElement element:error.getStackTrace()) stack.append('\n').append("at ").append(element);
        record(gatewayTransactionId,"ERROR_STACK",policy,CpfLogCaptureGuard.stack(stack.toString(),policy),stack.length());
    }

    private void record(String tx,String segment,LogPolicyDecision policy,CpfLogCaptureGuard.CapturedValue value,long observed) {
        if(value==null||value.value()==null||value.value().isEmpty()) return;
        ledger.recordCapture(new CpfGatewayLedgerPort.CaptureSegment(tx,segment,policy.schemaVersion(),
                policy.policyChecksum(),value.value(),value.truncated(),value.metadataOnly(),observed,OffsetDateTime.now()));
    }
    private long headerBytes(HttpHeaders headers) {
        if(headers==null) return 0;
        return headers.entrySet().stream().mapToLong(e->e.getKey().length()+e.getValue().stream().mapToInt(String::length).sum()).sum();
    }

    @FunctionalInterface private interface Completion { void accept(byte[] bytes,long total,boolean truncated); }
    private static final class CaptureInputStream extends FilterInputStream {
        private final int max; private final Completion completion; private final ByteArrayOutputStream capture;
        private final AtomicBoolean done=new AtomicBoolean(); private long total;
        CaptureInputStream(InputStream in,int max,Completion completion) {
            super(in);this.max=Math.max(0,max);this.completion=completion;this.capture=new ByteArrayOutputStream(Math.min(this.max,65536));
        }
        @Override public int read() throws IOException { int b=super.read(); if(b<0){finish();return -1;} total++; if(capture.size()<max)capture.write(b); return b; }
        @Override public int read(byte[] b,int off,int len)throws IOException {int n=super.read(b,off,len);if(n<0){finish();return -1;}total+=n;int remaining=max-capture.size();if(remaining>0)capture.write(b,off,Math.min(n,remaining));return n;}
        @Override public void close()throws IOException{try{super.close();}finally{finish();}}
        private void finish(){if(done.compareAndSet(false,true))completion.accept(capture.toByteArray(),total,total>max);}
    }
}
