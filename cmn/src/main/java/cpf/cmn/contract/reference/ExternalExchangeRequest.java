package cpf.cmn.contract.reference;

import jakarta.validation.constraints.Size;

/**
 * 업무 모듈이 EXS 외부 송신 원장에 전달하는 공개 요청 계약입니다.
 */
public record ExternalExchangeRequest(
        @Size(max = 120) String externalTransactionId,
        @Size(max = 50) String institutionCode,
        @Size(max = 30) String channelCode,
        @Size(max = 80) String endpointCode,
        @Size(max = 1000) String messageSummary) {
}
