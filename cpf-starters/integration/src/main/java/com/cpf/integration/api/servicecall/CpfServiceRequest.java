package com.cpf.integration.api.servicecall;

import java.util.LinkedHashMap;
import java.util.Map;

/** 업무/Runtime 모듈이 Core 내부 ServiceCallEngine에 직접 의존하지 않도록 제공하는 공개 요청 계약입니다. */
public record CpfServiceRequest(
        String serviceId,
        String endpointCode,
        String instanceId,
        String httpMethod,
        String requestPath,
        Integer timeoutMillis,
        Integer retryCount,
        Map<String,String> headers,
        Map<String,Object> attributes) {
    public CpfServiceRequest {
        if (serviceId == null || serviceId.isBlank()) throw new IllegalArgumentException("serviceId는 필수입니다.");
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    /** builder 작업을 CPF 표준 계약에 따라 수행한다. */
    public static Builder builder(String serviceId) { return new Builder(serviceId); }
    public static final class Builder {
        private final String serviceId; private String endpointCode; private String instanceId;
        private String httpMethod = "GET"; private String requestPath = "/";
        private Integer timeoutMillis; private Integer retryCount;
        private final Map<String,String> headers = new LinkedHashMap<>();
        private final Map<String,Object> attributes = new LinkedHashMap<>();
        private Builder(String serviceId) { this.serviceId = serviceId; }
        /** endpointCode 작업을 CPF 표준 계약에 따라 수행한다. */
        public Builder endpointCode(String v){ endpointCode=v; return this; }
        public Builder instanceId(String v){ instanceId=v; return this; }
        public Builder httpMethod(String v){ httpMethod=v; return this; }
        public Builder requestPath(String v){ requestPath=v; return this; }
        public Builder timeoutMillis(Integer v){ timeoutMillis=v; return this; }
        public Builder retryCount(Integer v){ retryCount=v; return this; }
        public Builder header(String k,String v){ if(k!=null&&v!=null) headers.put(k,v); return this; }
        /** attribute 작업을 CPF 표준 계약에 따라 수행한다. */
        public Builder attribute(String k,Object v){ if(k!=null&&v!=null) attributes.put(k,v); return this; }
        public CpfServiceRequest build(){ return new CpfServiceRequest(serviceId,endpointCode,instanceId,httpMethod,requestPath,timeoutMillis,retryCount,headers,attributes); }
    }
}
