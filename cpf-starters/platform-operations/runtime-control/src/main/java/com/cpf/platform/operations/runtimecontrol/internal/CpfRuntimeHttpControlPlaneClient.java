package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeAck;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeActualState;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeAgentPort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceLease;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceRegistration;
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
        this.client = client;
        this.agentToken = agentToken == null ? "" : agentToken;
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
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(type);
    }

    private record ActualStateRequest(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) { }
}
