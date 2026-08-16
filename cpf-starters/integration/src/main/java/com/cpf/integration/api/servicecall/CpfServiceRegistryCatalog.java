package com.cpf.integration.api.servicecall;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Service Registry Backend와 ADM UI가 함께 사용하는 제품 Code Catalog입니다. */
public final class CpfServiceRegistryCatalog {
    private CpfServiceRegistryCatalog() {}
    public static final List<String> SERVICE_TYPES = List.of("INTERNAL","EXTERNAL","PLATFORM","MONITOR_ONLY");
    public static final List<String> ENDPOINT_TYPES = List.of("HTTP","HTTPS","GRPC","TCP","WEBSOCKET","SSE","MONITOR_ONLY");
    public static final List<String> INSTANCE_STATUSES = List.of("UP","DOWN","DEGRADED","UNKNOWN","DRAINING","DISABLED","MAINTENANCE","STALE","RECOVERING");
    public static final List<String> ENVIRONMENTS = List.of("DEV","TEST","STG","PROD");

    /** requireServiceType 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String requireServiceType(String value) { return require(value,"INTERNAL",SERVICE_TYPES,"serviceType"); }
    public static String requireEndpointType(String value) { return require(value,"HTTP",ENDPOINT_TYPES,"endpointType"); }
    public static String requireEnvironment(String value) { return require(value,"DEV",ENVIRONMENTS,"environmentCode"); }

    private static String require(String value,String fallback,List<String> allowed,String field) {
        String code=value==null||value.isBlank()?fallback:value.trim().toUpperCase(Locale.ROOT);
        if(!allowed.contains(code)) throw new IllegalArgumentException(field+" is not supported: "+code);
        return code;
    }
}
