package com.cpf.integration.resilience.runtime;

import com.cpf.integration.api.annotation.CpfClient;
import com.cpf.integration.api.annotation.CpfRetry;
import com.cpf.integration.api.annotation.CpfTimeout;
import com.cpf.integration.api.annotation.CpfTimeLimiter;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/** Annotation을 Provider-neutral immutable resilience policy로 변환합니다. */
@SuppressWarnings("deprecation")
public final class CpfIntegrationAnnotationPolicyFactory {
    private final CpfIntegrationAnnotationProperties properties;
    public CpfIntegrationAnnotationPolicyFactory(CpfIntegrationAnnotationProperties properties){this.properties=properties;}
    public CpfResiliencePolicy create(Method method,CpfClient client,CpfRetry retry,CpfTimeout timeout,CpfTimeLimiter legacyTimeout){
        if(client==null)throw new IllegalArgumentException("CpfClient is required");
        String operation=operationId(method,client);
        long timeoutMs=properties.getDefaultTimeoutMillis();
        if(timeout!=null && timeout.timeoutMillis()>0) timeoutMs=positive(timeout.timeoutMillis(),"timeout.timeoutMillis");
        // Legacy @CpfTimeLimiter는 값 override가 없으므로 canonical config timeout을 그대로 사용합니다.
        if(timeout==null && legacyTimeout!=null && (legacyTimeout.name()==null || legacyTimeout.name().isBlank()))
            throw new IllegalArgumentException("legacy timeout.name is required");
        int attempts=retry==null?1:positive(retry.maxAttempts(),"retry.maxAttempts");
        long delay=retry==null?0:nonNegative(retry.delayMillis(),"retry.delayMillis");
        long revision=revision(operation,timeoutMs,attempts,delay,retry!=null&&retry.reconcileUnknownOutcome());
        return new CpfResiliencePolicy(operation,revision,Duration.ofMillis(timeoutMs),attempts,Duration.ofMillis(delay),
                properties.getCircuitFailureThreshold(),Duration.ofMillis(properties.getCircuitOpenMillis()),
                properties.getBulkheadMaxConcurrent(),properties.getRateLimitPermits(),Duration.ofMillis(properties.getRateLimitWindowMillis()),
                attempts>1,retry!=null&&retry.reconcileUnknownOutcome());
    }
    public String operationId(Method method,CpfClient client){
        String system=client.system()==null?"":client.system().trim();
        if(system.isEmpty())throw new IllegalArgumentException("CpfClient.system is required");
        String op=client.operation().isBlank()?method.getName():client.operation().trim();
        return "integration."+sanitize(system)+"."+sanitize(op);
    }
    private static String sanitize(String s){
        String v=s.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9._-]","-").replaceAll("-+","-");
        if(v.isBlank()||v.length()>96)throw new IllegalArgumentException("invalid integration operation token");return v;
    }
    private static int positive(int v,String n){if(v<1||v>1000)throw new IllegalArgumentException(n+" out of range");return v;}
    private static long positive(long v,String n){if(v<1||v>86_400_000L)throw new IllegalArgumentException(n+" out of range");return v;}
    private static long nonNegative(long v,String n){if(v<0||v>86_400_000L)throw new IllegalArgumentException(n+" out of range");return v;}
    private static long revision(String op,long timeout,int attempts,long delay,boolean reconcile){
        try{
            byte[] d=MessageDigest.getInstance("SHA-256").digest((op+'|'+timeout+'|'+attempts+'|'+delay+'|'+reconcile).getBytes(StandardCharsets.UTF_8));
            long v=0;for(int i=0;i<8;i++)v=(v<<8)|(d[i]&0xffL);return v&Long.MAX_VALUE;
        }catch(Exception e){throw new IllegalStateException(e);}
    }
}
