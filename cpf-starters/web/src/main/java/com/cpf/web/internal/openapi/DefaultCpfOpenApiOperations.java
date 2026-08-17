package com.cpf.web.internal.openapi;

import com.cpf.web.api.openapi.CpfOpenAPIOperations;
import com.cpf.web.api.openapi.CpfOpenAPISnapshot;
import com.cpf.web.api.openapi.CpfOpenAPIStatus;
import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Web MVC Route Inventory를 안전하게 집계하는 Web Profile 내부 구현입니다. */
final class DefaultCpfOpenAPIOperations implements CpfOpenAPIOperations {
    private final CpfOpenApiProperties properties;
    private final RequestMappingHandlerMapping mappings;
    private final Clock clock;
    private final AtomicReference<CpfOpenAPISnapshot> current = new AtomicReference<>();
    private final ReentrantLock refreshLock = new ReentrantLock();
    DefaultCpfOpenAPIOperations(CpfOpenApiProperties properties, RequestMappingHandlerMapping mappings, Clock clock){
        this.properties=Objects.requireNonNull(properties);this.mappings=Objects.requireNonNull(mappings);this.clock=Objects.requireNonNull(clock);
    }
    @Override public CpfOpenAPISnapshot snapshot(){ CpfOpenAPISnapshot value=current.get();return value==null?refresh("initial-inventory"):value; }
    @Override public CpfOpenAPISnapshot refresh(String reason){
        String auditedReason=requiredReason(reason); refreshLock.lock();
        try {
            CpfOpenAPISnapshot before=current.get(); Instant now=clock.instant();
            if(before!=null&&now.isBefore(before.refreshedAt().plus(properties.getMinimumRefreshInterval()))) return before;
            try {
                long count=mappings.getHandlerMethods().entrySet().stream()
                        .filter(e->hasRoutePattern(e.getKey()))
                        .filter(e->!e.getValue().getBeanType().getName().startsWith("org.springframework.boot.actuate"))
                        .count();
                CpfOpenAPISnapshot updated=snapshot(count>0?CpfOpenAPIStatus.UP:CpfOpenAPIStatus.DEGRADED,count,now,auditedReason,"");current.set(updated);return updated;
            } catch(RuntimeException failure){
                CpfOpenAPISnapshot updated=snapshot(CpfOpenAPIStatus.DOWN,0,now,auditedReason,failure.getClass().getSimpleName());current.set(updated);return updated;
            }
        } finally { refreshLock.unlock(); }
    }
    private static boolean hasRoutePattern(RequestMappingInfo info){ return !info.getPatternValues().isEmpty(); }
    private CpfOpenAPISnapshot snapshot(CpfOpenAPIStatus status,long count,Instant at,String reason,String failure){
        return new CpfOpenAPISnapshot(status,properties.isEnabled(),properties.isApiDocsEnabled(),properties.getApiDocsPath(),CpfInstanceIdentity.instanceId(),count,at,reason,failure);
    }
    private static String requiredReason(String value){ if(value==null||value.isBlank())throw new IllegalArgumentException("refresh reason is required");String v=value.trim();if(v.length()>500)throw new IllegalArgumentException("refresh reason is too long");return v; }
}
