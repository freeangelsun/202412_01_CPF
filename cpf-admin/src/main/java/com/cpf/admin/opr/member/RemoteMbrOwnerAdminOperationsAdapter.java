package com.cpf.admin.opr.member;

import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * 분리 WAS topology에서 ADM이 MBR Owner 운영 Port를 호출하는 Adapter입니다.
 * ServiceCall 공개 API를 사용하므로 Registry/timeout/retry/failover/trace/UNKNOWN 정책을 재사용합니다.
 */
public final class RemoteMbrOwnerAdminOperationsAdapter implements CpfOwnerAdminOperationsPort {
    private static final String SERVICE_ID = "MBR";
    private static final String ENDPOINT_CODE = "SMBRAD0001";
    private static final String BASE_PATH = "/mbr/internal/admin/operations";

    private final CpfServiceCaller caller;
    private final WebClient webClient;

    public RemoteMbrOwnerAdminOperationsAdapter(CpfServiceCaller caller, WebClient.Builder webClientBuilder) {
        this.caller = caller;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public String ownerSystemCode() {
        return SERVICE_ID;
    }

    @Override
    public Map<String, Object> query(CpfOwnerAdminQuery query) {
        return invoke("query", query);
    }

    @Override
    public Map<String, Object> command(CpfOwnerAdminCommand command) {
        return invoke("command", command);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invoke(String operation, Object payload) {
        String path = BASE_PATH + "/" + operation;
        CpfServiceRequest request = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE)
                .httpMethod("POST")
                .requestPath(path)
                .attribute("ownerDomain", "MBR")
                .attribute("callerDomain", "ADM")
                .build();
        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.post()
                .uri(join(target.baseUrl(), path))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Object.class)
                .block());
        if (result.unknown()) {
            throw new CpfBusinessException(
                    CpfErrorCode.EXTERNAL_SERVICE_ERROR,
                    "MBR Owner 운영 명령 결과를 확정할 수 없습니다. reconciliation이 필요합니다. code="
                            + result.failureCode());
        }
        if (!result.success()) {
            throw new CpfBusinessException(
                    CpfErrorCode.EXTERNAL_SERVICE_ERROR,
                    "MBR Owner 호출 실패. status=" + result.status()
                            + ", code=" + result.failureCode()
                            + ", message=" + result.failureMessage());
        }
        Object body = result.responseBody();
        return body instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String join(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new CpfBusinessException(CpfErrorCode.EXTERNAL_SERVICE_ERROR, "MBR service baseUrl을 확인할 수 없습니다.");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + path : baseUrl + path;
    }
}
