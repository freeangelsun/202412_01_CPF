package com.cpf.gateway.service;

import com.cpf.core.api.gateway.*;
import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import com.cpf.core.api.servicecall.*;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.channel.model.CpfChannelPolicyDecision;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

/** route·registry·인증·header 신뢰경계를 적용해 대상 서비스로 요청을 전달합니다. */
@Service
public class CpfGatewayProxyService {
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length");
    private static final Set<String> NEVER_FORWARD = Set.of(
            CpfHeaderNames.AUTHORIZATION.toLowerCase(Locale.ROOT),
            CpfHeaderNames.API_KEY.toLowerCase(Locale.ROOT));
    private static final Set<String> PASSTHROUGH = Set.of(
            HttpHeaders.ACCEPT.toLowerCase(Locale.ROOT), HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(Locale.ROOT),
            HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ROOT), HttpHeaders.USER_AGENT.toLowerCase(Locale.ROOT),
            CpfHeaderNames.ORIGINAL_CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.CHANNEL_CODE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.REQUEST_TYPE.toLowerCase(Locale.ROOT),
            CpfHeaderNames.REQUEST_SIGNATURE.toLowerCase(Locale.ROOT));

    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfServiceCallExecutor serviceCallEngine;
    private final CpfGatewayAuthenticationPort authenticationPort;
    private final CpfGatewayAuthorizationPort authorizationPort;
    private final CpfGatewayAuditPort auditPort;
    private final CpfChannelPolicyService channelPolicyService;
    private final CpfGatewayRuntimePolicy runtimePolicy;
    private final RestClient restClient;

    public CpfGatewayProxyService(
            CpfGatewayRouteSnapshot snapshot, CpfServiceCallExecutor serviceCallEngine,
            CpfGatewayAuthenticationPort authenticationPort, CpfGatewayAuthorizationPort authorizationPort,
            CpfGatewayAuditPort auditPort, CpfChannelPolicyService channelPolicyService,
            CpfGatewayRuntimePolicy runtimePolicy, RestClient restClient) {
        this.snapshot=snapshot; this.serviceCallEngine=serviceCallEngine; this.authenticationPort=authenticationPort;
        this.authorizationPort=authorizationPort; this.auditPort=auditPort; this.channelPolicyService=channelPolicyService;
        this.runtimePolicy=runtimePolicy; this.restClient=restClient;
    }

    /** 기존 내부 Consumer 호환용 POST 진입점입니다. */
    public ResponseEntity<byte[]> execute(String executionId, HttpHeaders inboundHeaders, byte[] body) {
        return execute(executionId, "POST", inboundHeaders, body, "", "");
    }

    public ResponseEntity<byte[]> execute(String executionId, String inboundMethod, HttpHeaders inboundHeaders, byte[] body) {
        return execute(executionId, inboundMethod, inboundHeaders, body, "", "");
    }

    public ResponseEntity<byte[]> execute(String executionId, String inboundMethod, HttpHeaders inboundHeaders, byte[] body,
                                          String verifiedClientIp, String verifiedCertificateSerial) {
        CpfGatewayRoute route=snapshot.resolve(executionId);
        HttpMethod method=httpMethod(inboundMethod);
        if (!method.equals(httpMethod(route.httpMethod()))) {
            throw new IllegalArgumentException("Gateway route HTTP method 불일치. inbound="+method+", route="+route.httpMethod());
        }
        CpfGatewayRuntimePolicy.CorsDecision corsDecision = corsDecision(inboundHeaders, method.name());
        if (!corsDecision.allowed()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Gateway CORS 정책 거부: " + corsDecision.reason());
        Map<String,String> credentials=credentialHeaders(inboundHeaders, verifiedClientIp, verifiedCertificateSerial);
        CpfGatewayPrincipal principal=Objects.requireNonNullElse(authenticationPort.authenticate(route, credentials), CpfGatewayPrincipal.anonymous());
        if (!runtimePolicy.tryAcquire(route.standardExecutionId(), principal.principalId(), inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Gateway rate limit 초과");
        }
        Map<String,String> trusted=trustedHeaders(inboundHeaders, principal);
        CpfChannelPolicyDecision channelDecision=channelPolicyService.evaluate(
                route.standardExecutionId(), inboundHeaders.getFirst(CpfHeaderNames.ORIGINAL_CHANNEL_CODE),
                inboundHeaders.getFirst(CpfHeaderNames.CHANNEL_CODE), inboundHeaders.getFirst(CpfHeaderNames.REQUEST_TYPE),
                principal.authenticated(), inboundHeaders.containsKey(CpfHeaderNames.REQUEST_SIGNATURE));
        if (!channelDecision.allowed()) throw new SecurityException("Gateway 채널 정책에서 요청을 거부했습니다. reason="+channelDecision.reason());
        if (route.requiredPermission()!=null && !route.requiredPermission().isBlank() && !principal.authenticated()) {
            throw new SecurityException("보호 Gateway route에는 검증된 Principal이 필요합니다.");
        }
        if (!authorizationPort.isAllowed(route, trusted)) throw new SecurityException("Gateway route 실행 권한이 없습니다. permission="+route.requiredPermission());

        String auditReason=trimToNull(inboundHeaders.getFirst(CpfHeaderNames.AUDIT_REASON));
        if (route.auditReasonRequired()) {
            if (auditReason==null) throw new IllegalArgumentException("Gateway 위험 거래에는 "+CpfHeaderNames.AUDIT_REASON+"가 필요합니다.");
            if (!auditPort.durable()) throw new IllegalStateException("Gateway 위험 거래용 durable Audit adapter가 구성되지 않았습니다.");
            audit(route,principal,auditReason,"PRE_DISPATCH","ACCEPTED",null,null);
        }

        CpfServiceCallCommand callRequest=CpfServiceCallCommand.builder(route.serviceId())
                .httpMethod(route.httpMethod()).requestPath(route.endpoint())
                .attribute("standardExecutionId",route.standardExecutionId()).build();
        HttpHeaders outbound=outboundHeaders(inboundHeaders,route);
        try {
            CpfServiceCallOutcome<ResponseEntity<byte[]>> result=serviceCallEngine.invoke(callRequest, target -> invokeTarget(target,route,outbound,body));
            if (!"SUCCESS".equals(result.status()) || result.responseBody()==null) throw new CpfServiceCallFailedException(result);
            ResponseEntity<byte[]> response=withGatewayResponseHeaders(result.responseBody(),route,corsDecision);
            if (route.auditReasonRequired()) audit(route,principal,auditReason,"POST_DISPATCH","SUCCESS",result.target()==null?null:result.target().instanceId(),response.getStatusCode().value());
            return response;
        } catch (RuntimeException ex) {
            if (route.auditReasonRequired()) {
                try { audit(route,principal,auditReason,"POST_DISPATCH","FAILED",null,null); }
                catch (RuntimeException auditFailure) { ex.addSuppressed(auditFailure); }
            }
            throw ex;
        }
    }

    public ResponseEntity<byte[]> preflight(String executionId, HttpHeaders inboundHeaders) {
        CpfGatewayRoute route = snapshot.resolve(executionId);
        String requestedMethod = inboundHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        CpfGatewayRuntimePolicy.CorsDecision decision = corsDecision(inboundHeaders, requestedMethod == null ? route.httpMethod() : requestedMethod);
        if (!decision.allowed()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Gateway CORS 정책 거부: " + decision.reason());
        HttpHeaders response = new HttpHeaders();
        applyCorsHeaders(response, decision);
        response.setAccessControlAllowMethods(List.of(httpMethod(route.httpMethod()), HttpMethod.OPTIONS));
        List<String> requestHeaders = accessControlRequestHeaders(inboundHeaders);
        if (!requestHeaders.isEmpty()) response.setAccessControlAllowHeaders(requestHeaders);
        return new ResponseEntity<>(new byte[0], response, HttpStatus.NO_CONTENT);
    }

    private CpfGatewayRuntimePolicy.CorsDecision corsDecision(HttpHeaders headers, String method) {
        return runtimePolicy.evaluateCors(headers.getOrigin(), method, accessControlRequestHeaders(headers));
    }
    private List<String> accessControlRequestHeaders(HttpHeaders headers) {
        List<String> values = headers.get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (values == null || values.isEmpty()) return List.of();
        ArrayList<String> result = new ArrayList<>();
        for (String value : values) for (String part : value.split(",")) if (!part.isBlank()) result.add(part.trim());
        return List.copyOf(result);
    }
    private void applyCorsHeaders(HttpHeaders headers, CpfGatewayRuntimePolicy.CorsDecision decision) {
        if (decision == null || decision.allowOrigin().isBlank()) return;
        headers.setAccessControlAllowOrigin(decision.allowOrigin());
        headers.setAccessControlAllowCredentials(decision.allowCredentials());
        headers.setAccessControlMaxAge(decision.maxAgeSeconds());
        if (!decision.exposedHeaders().isEmpty()) headers.setAccessControlExposeHeaders(new ArrayList<>(decision.exposedHeaders()));
        headers.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }

    private ResponseEntity<byte[]> invokeTarget(CpfServiceCallTarget target,CpfGatewayRoute route,HttpHeaders outbound,byte[] body) {
        URI uri=targetUri(target.baseUrl(),route.endpoint());
        RestClient.RequestBodySpec spec=restClient.method(httpMethod(route.httpMethod())).uri(uri).headers(h->h.putAll(outbound));
        if (body!=null && body.length>0 && !HttpMethod.GET.matches(route.httpMethod())) spec.body(body);
        return spec.retrieve().toEntity(byte[].class);
    }

    private ResponseEntity<byte[]> withGatewayResponseHeaders(ResponseEntity<byte[]> downstream,CpfGatewayRoute route,CpfGatewayRuntimePolicy.CorsDecision corsDecision) {
        HttpHeaders headers=new HttpHeaders();
        downstream.getHeaders().forEach((name,values)->{ String lower=name.toLowerCase(Locale.ROOT); if(!HOP_BY_HOP.contains(lower) && runtimePolicy.allowResponseHeader(lower)) headers.put(name,values); });
        headers.set(CpfHeaderNames.GATEWAY_INSTANCE_ID,CpfInstanceIdentity.current().serverInstanceId());
        headers.set(CpfHeaderNames.GATEWAY_ROUTE_ID,route.standardExecutionId());
        headers.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION,route.routeVersion());
        applyCorsHeaders(headers,corsDecision);
        return new ResponseEntity<>(downstream.getBody(),headers,downstream.getStatusCode());
    }

    private HttpHeaders outboundHeaders(HttpHeaders inbound,CpfGatewayRoute route) {
        HttpHeaders result=new HttpHeaders();
        inbound.forEach((name,values)->{
            String lower=name.toLowerCase(Locale.ROOT);
            if (PASSTHROUGH.contains(lower) && runtimePolicy.allowRequestHeader(lower)
                    && !HOP_BY_HOP.contains(lower) && !NEVER_FORWARD.contains(lower)) result.put(name,List.copyOf(values));
        });
        result.set(CpfHeaderNames.STANDARD_EXECUTION_ID,route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_INSTANCE_ID,CpfInstanceIdentity.current().serverInstanceId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_ID,route.standardExecutionId());
        result.set(CpfHeaderNames.GATEWAY_ROUTE_VERSION,route.routeVersion());
        result.set(CpfHeaderNames.INGRESS_TYPE,"CPF_GATEWAY");
        return result;
    }

    private Map<String,String> credentialHeaders(HttpHeaders headers,String verifiedClientIp,String verifiedCertificateSerial) {
        Map<String,String> result=new LinkedHashMap<>();
        copyFirst(headers,result,CpfHeaderNames.AUTHORIZATION); copyFirst(headers,result,CpfHeaderNames.API_KEY);
        copyFirst(headers,result,CpfHeaderNames.REQUEST_SIGNATURE);
        if(verifiedClientIp!=null&&!verifiedClientIp.isBlank())result.put("cpf.client.ip",verifiedClientIp.trim());
        if(verifiedCertificateSerial!=null&&!verifiedCertificateSerial.isBlank())result.put("cpf.client.cert.serial",verifiedCertificateSerial.trim());
        return Map.copyOf(result);
    }

    private Map<String,String> trustedHeaders(HttpHeaders headers,CpfGatewayPrincipal principal) {
        Map<String,String> result=new LinkedHashMap<>();
        for(String name:List.of(CpfHeaderNames.ORIGINAL_CHANNEL_CODE,CpfHeaderNames.CHANNEL_CODE,CpfHeaderNames.REQUEST_TYPE,CpfHeaderNames.STANDARD_EXECUTION_ID)) copyFirst(headers,result,name);
        if(principal.authenticated()) { result.put("cpf.principal.id",principal.principalId()); result.put("cpf.principal.authorities",String.join(",",principal.authorities())); }
        principal.attributes().forEach((k,v)->result.put("cpf.principal."+k,v));
        return Map.copyOf(result);
    }

    private void audit(CpfGatewayRoute route,CpfGatewayPrincipal principal,String reason,String phase,String outcome,String target,Integer status) {
        auditPort.record(new CpfGatewayAuditEvent(CpfTransactionContext.transactionId(),route.standardExecutionId(),principal.principalId(),reason,phase,outcome,target,status,Instant.now(),Map.of("routeVersion",Objects.toString(route.routeVersion(),""))));
    }

    private URI targetUri(String baseUrl,String endpoint) {
        try {
            URI base=new URI(requireText(baseUrl,"baseUrl").trim());
            if (!("http".equalsIgnoreCase(base.getScheme())||"https".equalsIgnoreCase(base.getScheme())) || base.getHost()==null || base.getUserInfo()!=null || base.getQuery()!=null || base.getFragment()!=null)
                throw new IllegalArgumentException("Gateway 대상 baseUrl은 userInfo/query/fragment 없는 http(s) absolute URI여야 합니다.");
            String path=normalizePath(endpoint);
            if(path.contains("..") || path.chars().anyMatch(ch->ch<0x20 || ch==0x7f)) throw new IllegalArgumentException("Gateway endpoint path가 안전하지 않습니다.");
            String basePath=base.getPath()==null?"":base.getPath(); while(basePath.endsWith("/"))basePath=basePath.substring(0,basePath.length()-1);
            return new URI(base.getScheme(),null,base.getHost(),base.getPort(),basePath+path,null,null);
        } catch (URISyntaxException ex) { throw new IllegalArgumentException("Gateway 대상 URI가 올바르지 않습니다.",ex); }
    }

    private HttpMethod httpMethod(String value) {
        String normalized=requireText(value,"httpMethod").toUpperCase(Locale.ROOT);
        HttpMethod method=HttpMethod.valueOf(normalized);
        if (!(method==HttpMethod.GET||method==HttpMethod.POST||method==HttpMethod.PUT||method==HttpMethod.PATCH||method==HttpMethod.DELETE)) throw new IllegalArgumentException("허용되지 않은 Gateway HTTP method: "+value);
        return method;
    }
    private String normalizePath(String value){String v=value==null||value.isBlank()?"/":value.trim();return v.startsWith("/")?v:"/"+v;}
    private String requireText(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+"가 필요합니다.");return value;}
    private String trimToNull(String value){return value==null||value.isBlank()?null:value.trim();}
    private void copyFirst(HttpHeaders source,Map<String,String> target,String name){String value=source.getFirst(name);if(value!=null)target.put(name,value);}
}
