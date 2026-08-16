package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.result.CpfResultStatus;
import com.cpf.integration.api.domaincall.CpfDomainRemoteTransport;
import com.cpf.integration.http.internal.CpfWebClient;
import com.cpf.integration.http.internal.servicecall.ServiceCallResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

/** 기존 ServiceCallEngine/Registry/Health-aware Routing을 재사용하는 Remote Domain HTTP adapter입니다. */
public final class CpfHttpDomainRemoteTransport implements CpfDomainRemoteTransport {
    private final CpfWebClient webClient;
    private final ObjectMapper objectMapper;
    public CpfHttpDomainRemoteTransport(CpfWebClient webClient, ObjectMapper objectMapper) {
        this.webClient = Objects.requireNonNull(webClient, "webClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType) {
        String serviceId = binding.serviceId();
        if (serviceId == null || serviceId.isBlank()) {
            return CpfResult.technicalFailure("CPF-DOMAIN-SERVICE-ID-MISSING", systemCode + " serviceId가 없습니다.");
        }
        ServiceCallResult<CpfDomainRemoteEnvelope> transport = webClient.postResult(
                serviceId, "/_cpf/domain/" + systemCode + "/" + operationId, request, CpfDomainRemoteEnvelope.class);
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
