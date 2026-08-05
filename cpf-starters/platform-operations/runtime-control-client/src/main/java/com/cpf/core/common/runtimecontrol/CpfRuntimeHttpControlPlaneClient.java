package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeAck;
import com.cpf.core.api.runtimecontrol.CpfRuntimeActualState;
import com.cpf.core.api.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.core.api.runtimecontrol.CpfRuntimeInstanceRegistration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** 분리 WAS/MSA Runtime Agent가 ADM Control Plane과 동일 Public API 계약으로 통신하는 HTTP adapter입니다. */
public class CpfRuntimeHttpControlPlaneClient implements CpfRuntimeAgentPort {
    private static final String AGENT_TOKEN_HEADER = "X-Cpf-Runtime-Agent-Token";
    private final RestClient client;
    private final String agentToken;

    public CpfRuntimeHttpControlPlaneClient(RestClient client, String agentToken) {
        this.client = java.util.Objects.requireNonNull(client, "client");
        this.agentToken = normalizeAgentToken(agentToken);
    }

    @Override
    public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration) {
        return post("/cpf/runtime-control/agent/register", registration, CpfRuntimeInstanceLease.class);
    }

    @Override
    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash, long actualVersion) {
        return heartbeat(instanceId, fencingToken, actualHash, actualVersion, Instant.now());
    }

    @Override
    public CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                              long actualVersion, Instant agentTime) {
        return post("/cpf/runtime-control/agent/heartbeat",
                Map.of("instanceId", instanceId, "fencingToken", fencingToken,
                        "actualHash", actualHash == null ? "" : actualHash,
                        "actualVersion", actualVersion,
                        "agentTime", (agentTime == null ? Instant.now() : agentTime).toString()),
                CpfRuntimeInstanceLease.class);
    }

    @Override
    public List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int limit) {
        CpfRuntimeDelivery[] result = post("/cpf/runtime-control/agent/claim",
                Map.of("instanceId", instanceId, "fencingToken", fencingToken, "limit", limit),
                CpfRuntimeDelivery[].class);
        return result == null ? List.of() : List.of(result);
    }

    @Override
    public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack) {
        return post("/cpf/runtime-control/agent/ack", ack, CpfRuntimeChangeResult.class);
    }

    @Override
    public void deregister(String instanceId, long fencingToken, String reason) {
        post("/cpf/runtime-control/agent/deregister",
                Map.of("instanceId", instanceId, "fencingToken", fencingToken,
                        "reason", reason == null ? "" : reason), Void.class);
    }

    @Override
    public void reconcileActualState(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
        post("/cpf/runtime-control/agent/actual-state",
                new ActualStateRequest(instanceId, fencingToken, states == null ? List.of() : states), Void.class);
    }

    private <T> T post(String uri, Object body, Class<T> type) {
        return client.post().uri(uri).header(AGENT_TOKEN_HEADER, agentToken)
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve()
                .onStatus(CpfRuntimeHttpControlPlaneClient::isFencingStatus, (request, response) -> {
                    throw new com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException(
                            "Remote Runtime Control Plane이 stale/invalid fencing token을 거부했습니다.");
                })
                .onStatus(CpfRuntimeHttpControlPlaneClient::isRateLimitStatus, (request, response) -> {
                    throw new com.cpf.core.api.runtimecontrol.CpfRuntimeRateLimitException(
                            "Remote Runtime Control Plane rate limit을 초과했습니다.");
                })
                .body(type);
    }

    static boolean isFencingStatus(org.springframework.http.HttpStatusCode status) {
        return status != null && status.value() == 409;
    }

    static boolean isRateLimitStatus(org.springframework.http.HttpStatusCode status) {
        return status != null && status.value() == 429;
    }

    static String normalizeAgentToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Runtime Agent HTTP client에는 agentToken이 필요합니다.");
        }
        String normalized=token.trim();
        if(normalized.length()>2048){
            throw new IllegalArgumentException("Runtime Agent agentToken은 최대 2048자입니다.");
        }
        for(int i=0;i<normalized.length();i++){
            char ch=normalized.charAt(i);
            if(Character.isISOControl(ch)){
                throw new IllegalArgumentException("Runtime Agent agentToken에는 제어문자를 사용할 수 없습니다.");
            }
        }
        return normalized;
    }

    private record ActualStateRequest(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) { }
}
