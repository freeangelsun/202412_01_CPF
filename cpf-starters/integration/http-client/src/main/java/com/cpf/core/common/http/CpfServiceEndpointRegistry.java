package com.cpf.core.common.http;

import com.cpf.core.common.exception.CpfFrameworkErrorCode;
import com.cpf.core.common.exception.CpfFrameworkException;
import com.cpf.core.api.security.network.CpfNetworkEndpointPolicy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** application 설정과 Runtime 기관 endpoint snapshot을 함께 해석합니다. */
public class CpfServiceEndpointRegistry {
    private final Map<String, CpfServiceEndpointProperties.ServiceEndpoint> configured;
    private final AtomicReference<Snapshot> runtime = new AtomicReference<>(Snapshot.empty());
    private final CpfNetworkEndpointPolicy endpointPolicy;
    public CpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        this(properties, CpfNetworkEndpointPolicy.secureDefault());
    }
    public CpfServiceEndpointRegistry(CpfServiceEndpointProperties properties, CpfNetworkEndpointPolicy endpointPolicy) {
        this.configured = properties == null || properties.getServices() == null ? Map.of() : Map.copyOf(properties.getServices());
        this.endpointPolicy = Objects.requireNonNull(endpointPolicy, "endpointPolicy");
    }
    public String baseUrl(String serviceId) {
        String id = normalize(serviceId); RuntimeEndpoint dynamic = runtime.get().endpoints().get(id);
        if (dynamic != null) { if (!dynamic.active() || dynamic.maintenance()) throw missing(id,"Runtime endpoint가 비활성/점검 상태입니다."); return validatedBaseUrl(dynamic.baseUrl(), dynamic.attributes()); }
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = configured.get(id);
        if (endpoint == null || !hasText(endpoint.getBaseUrl())) throw missing(id,"Service endpoint is not configured.");
        return validatedBaseUrl(endpoint.getBaseUrl(), Map.of());
    }
    public RuntimeEndpoint runtimeEndpoint(String serviceId) { return runtime.get().endpoints().get(normalize(serviceId)); }
    public Snapshot runtimeSnapshot() { return runtime.get(); }
    public Snapshot replaceRuntime(long version, Map<String, RuntimeEndpoint> endpoints) {
        if (version < 0) throw new IllegalArgumentException("endpoint registry version 범위 오류");
        LinkedHashMap<String, RuntimeEndpoint> normalized = new LinkedHashMap<>();
        if (endpoints != null) endpoints.forEach((key,value) -> { if (value == null) throw new IllegalArgumentException("null endpoint 금지"); String
                id=normalize(key==null||key.isBlank()?value.serviceId():key); RuntimeEndpoint candidate=value.normalize(id); if(normalized.putIfAbsent(id,candidate)!=null)throw new
                IllegalArgumentException("service endpoint 중복"); });
        while(true){Snapshot old=runtime.get();if(version<old.version())throw new IllegalArgumentException("endpoint registry version 역행 금지");Snapshot next=new Snapshot(version,
                Map.copyOf(normalized));if(runtime.compareAndSet(old,next))return next;}
    }
    private CpfFrameworkException missing(String id,String message){return new CpfFrameworkException(CpfFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND,message,Map.of("serviceId",id));}
    private String normalize(String value){if(!hasText(value))throw missing("EMPTY","Service id is required.");return value.trim().toLowerCase(Locale.ROOT);}
    private String validatedBaseUrl(String value, Map<String,String> attributes){
        if(!hasText(value))throw new IllegalArgumentException("baseUrl 필수");
        String v=value.trim();while(v.endsWith("/"))v=v.substring(0,v.length()-1);
        boolean allowDns=attributes!=null&&Boolean.parseBoolean(attributes.getOrDefault("allowDns","false"));
        boolean allowPrivate=attributes!=null&&Boolean.parseBoolean(attributes.getOrDefault("allowPrivate","false"));
        CpfNetworkEndpointPolicy policy=(allowDns||allowPrivate)
                ?new CpfNetworkEndpointPolicy(List.of(),List.of(443,8443,9443),allowPrivate,allowDns,true)
                :endpointPolicy;
        policy.validateEndpoint(v);return v;
    }
    private boolean hasText(String value){return value!=null&&!value.isBlank();}
    public record RuntimeEndpoint(String serviceId,String endpointType,String baseUrl,String credentialRef,String layoutId,String layoutVersion,int timeoutMillis,boolean active,boolean
            maintenance,Map<String,String>attributes){public RuntimeEndpoint{attributes=attributes==null?Map.of():Map.copyOf(attributes);if(timeoutMillis<1||timeoutMillis>300000)throw new
            IllegalArgumentException("external timeout 범위 오류");}private RuntimeEndpoint normalize(String id){return new RuntimeEndpoint(id,Objects.toString(endpointType,
            "HTTP").trim().toUpperCase(Locale.ROOT),baseUrl==null?"":baseUrl.trim(),credentialRef==null?"":credentialRef.trim(),layoutId==null?"":layoutId.trim(),
            layoutVersion==null?"":layoutVersion.trim(),timeoutMillis,active,maintenance,attributes);}}
    public record Snapshot(long version,Map<String,RuntimeEndpoint>endpoints){public Snapshot{endpoints=endpoints==null?Map.of():Map.copyOf(endpoints);}private static Snapshot empty(){return new Snapshot(0,Map.of());}}
}
