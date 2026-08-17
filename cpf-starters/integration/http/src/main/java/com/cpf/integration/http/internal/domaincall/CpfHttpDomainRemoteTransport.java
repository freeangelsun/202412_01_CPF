package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.result.CpfResultStatus;
import com.cpf.integration.api.domaincall.CpfDomainRemoteTransport;
import com.cpf.integration.api.domaincall.CpfDomainCallOptions;
import com.cpf.integration.http.internal.CpfWebClient;
import com.cpf.integration.http.internal.servicecall.ServiceCallRequest;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import com.cpf.web.context.CpfHttpOutboundRequest;
import com.cpf.web.context.CpfWebContexts;
import com.cpf.integration.http.internal.servicecall.ServiceCallResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

/** 기존 ServiceCallEngine/Registry/Health-aware Routing을 재사용하는 Remote Domain HTTP adapter입니다. */
public final class CpfHttpDomainRemoteTransport implements CpfDomainRemoteTransport {
    private final CpfWebClient webClient;
    private final ObjectMapper objectMapper;
    private final CpfHttpOutboundContextAdapter outboundHeaders;
    public CpfHttpDomainRemoteTransport(CpfWebClient webClient, ObjectMapper objectMapper,
                                        CpfHttpOutboundContextAdapter outboundHeaders) {
        this.webClient = Objects.requireNonNull(webClient, "webClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.outboundHeaders = Objects.requireNonNull(outboundHeaders, "outboundHeaders");
    }

    @Override
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType) {
        return invoke(systemCode, operationId, binding, request, responseType, CpfDomainCallOptions.none());
    }

    @Override
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType,
            CpfDomainCallOptions options) {
        String serviceId = binding.serviceId();
        if (serviceId == null || serviceId.isBlank()) {
            return CpfResult.technicalFailure("CPF-DOMAIN-SERVICE-ID-MISSING", systemCode + " serviceId가 없습니다.");
        }
        var headers = outboundHeaders.headers(
                CpfContexts.requireCurrent(),
                CpfWebContexts.current(),
                new CpfHttpOutboundRequest(systemCode, operationId, null, true,
                        options == null ? java.util.Map.of() : options.headers()));
        ServiceCallRequest.Builder call = ServiceCallRequest.builder(serviceId)
                .endpointCode(operationId)
                .httpMethod("POST")
                .requestPath("/_cpf/domain/" + systemCode + "/" + operationId);
        headers.forEach(call::header);
        ServiceCallResult<CpfDomainRemoteEnvelope> transport = webClient.postResult(
                call.build(), request, CpfDomainRemoteEnvelope.class);
        if (!transport.successValue()) {
            return mapTransportFailure(transport);
        }
        CpfDomainRemoteEnvelope envelope = transport.responseBody();
        if (envelope == null || envelope.status() == null) {
            return CpfResult.technicalFailure("CPF-DOMAIN-INVALID-RESPONSE", "Domain 응답 envelope가 없습니다.");
        }
        if (envelope.status() == CpfResultStatus.SUCCESS) {
            return CpfResult.success(objectMapper.convertValue(envelope.data(), responseType));
        }
        if (envelope.status() == CpfResultStatus.BUSINESS_FAILURE) {
            return CpfResult.businessFailure(envelope.errorCode(), envelope.errorMessage());
        }
        if (envelope.status() == CpfResultStatus.UNKNOWN) {
            String recoveryId = envelope.recoveryId();
            if (recoveryId == null || recoveryId.isBlank()) recoveryId = transport.recoveryId();
            if (recoveryId == null || recoveryId.isBlank()) recoveryId = "DOMAIN-UNKNOWN-" + systemCode + "-" + operationId;
            return CpfResult.unknown(envelope.errorCode(), envelope.errorMessage(), new CpfRecoveryInfo(recoveryId, envelope.recoveryAction()));
        }
        return CpfResult.technicalFailure(envelope.errorCode(), envelope.errorMessage());
    }

    private static <O extends CpfResponse> CpfResult<O> mapTransportFailure(ServiceCallResult<?> result) {
        if (result.unknownValue()) {
            String recoveryId = result.recoveryId();
            if (recoveryId == null || recoveryId.isBlank()) recoveryId = "DOMAIN-UNKNOWN-TRANSPORT";
            return CpfResult.unknown(result.failureCode(), result.failureMessage(), new CpfRecoveryInfo(recoveryId, result.recoveryAction()));
        }
        if (result.businessFailureValue()) return CpfResult.businessFailure(result.failureCode(), result.failureMessage());
        return CpfResult.technicalFailure(result.failureCode(), result.failureMessage());
    }
}
