package com.cpf.integration.api.servicecall;

import java.util.LinkedHashMap;
import java.util.Map;

/** 외부 모듈이 CPF 표준 서비스 호출 엔진에 전달하는 topology-independent 호출 명령입니다. */
public record CpfServiceCallCommand(
        String serviceId, String httpMethod, String requestPath, Integer timeoutMillis, Integer retryCount,
        Map<String,String> headers, Map<String,Object> attributes) {
    public CpfServiceCallCommand {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    /** builder 작업을 CPF 표준 계약에 따라 수행한다. */
    public static Builder builder(String serviceId) { return new Builder(serviceId); }
    public static final class Builder {
        private final String serviceId; private String httpMethod="GET"; private String requestPath="/";
        private Integer timeoutMillis; private Integer retryCount;
        private final Map<String,String> headers=new LinkedHashMap<>();
        private final Map<String,Object> attributes=new LinkedHashMap<>();
        private Builder(String serviceId){this.serviceId=serviceId;}
        /** httpMethod 작업을 CPF 표준 계약에 따라 수행한다. */
        public Builder httpMethod(String v){this.httpMethod=v;return this;}
        public Builder requestPath(String v){this.requestPath=v;return this;}
        public Builder timeoutMillis(Integer v){this.timeoutMillis=v;return this;}
        public Builder retryCount(Integer v){this.retryCount=v;return this;}
        public Builder header(String k,String v){if(k!=null&&v!=null)headers.put(k,v);return this;}
        public Builder attribute(String k,Object v){if(k!=null&&v!=null)attributes.put(k,v);return this;}
        public CpfServiceCallCommand build(){return new CpfServiceCallCommand(serviceId,httpMethod,requestPath,timeoutMillis,retryCount,headers,attributes);}
    }
}
