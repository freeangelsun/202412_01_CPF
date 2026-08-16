package com.cpf.web.internal.openapi;

import com.cpf.web.api.openapi.CpfOpenApiOperations;
import com.cpf.web.api.openapi.CpfOpenApiSnapshot;
import com.cpf.web.api.openapi.CpfOpenApiStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Web MVC Route Inventory를 안전하게 집계하는 Web Profile 내부 구현입니다. */
final class DefaultCpfOpenApiOperations implements CpfOpenApiOperations {
    private final CpfOpenApiProperties properties;
    private final RequestMappingHandlerMapping mappings;
    private final Clock clock;
    private final AtomicReference<CpfOpenApiSnapshot> current = new AtomicReference<>();
    private final ReentrantLock refreshLock = new ReentrantLock();
    DefaultCpfOpenApiOperations(CpfOpenApiProperties properties, RequestMappingHandlerMapping mappings, Clock clock){
        this.properties=Objects.requireNonNull(properties);this.mappings=Objects.requireNonNull(mappings);this.clock=Objects.requireNonNull(clock);
    }
    @Override public CpfOpenApiSnapshot snapshot(){ CpfOpenApiSnapshot value=current.get();return value==null?refresh("initial-inventory"):value; }
    @Override public CpfOpenApiSnapshot refresh(String reason){
        String auditedReason=requiredReason(reason); refreshLock.lock();
        try {
            CpfOpenApiSnapshot before=current.get(); Instant now=clock.instant();
            if(before!=null&&now.isBefore(before.refreshedAt().plus(properties.getMinimumRefreshInterval()))) return before;
            try {
                long count=mappings.getHandlerMethods().entrySet().stream()
                        .filter(e->hasRoutePattern(e.getKey()))
                        .filter(e->!e.getValue().getBeanType().getName().startsWith("org.springframework.boot.actuate"))
                        .count();
                CpfOpenApiSnapshot updated=snapshot(count>0?CpfOpenApiStatus.UP:CpfOpenApiStatus.DEGRADED,count,now,auditedReason,"");current.set(updated);return updated;
            } catch(RuntimeException failure){
                CpfOpenApiSnapshot updated=snapshot(CpfOpenApiStatus.DOWN,0,now,auditedReason,failure.getClass().getSimpleName());current.set(updated);return updated;
            }
        } finally { refreshLock.unlock(); }
    }
    private static boolean hasRoutePattern(RequestMappingInfo info){ return !info.getPatternValues().isEmpty(); }
    private CpfOpenApiSnapshot snapshot(CpfOpenApiStatus status,long count,Instant at,String reason,String failure){
        return new CpfOpenApiSnapshot(status,properties.isEnabled(),properties.isApiDocsEnabled(),properties.getApiDocsPath(),properties.getInstanceId(),count,at,reason,failure);
    }
    private static String requiredReason(String value){ if(value==null||value.isBlank())throw new IllegalArgumentException("refresh reason is required");String v=value.trim();if(v.length()>500)throw new IllegalArgumentException("refresh reason is too long");return v; }
}
