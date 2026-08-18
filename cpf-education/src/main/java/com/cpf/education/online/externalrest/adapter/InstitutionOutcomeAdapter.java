package com.cpf.education.online.externalrest.adapter;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.externalrest.dto.InstitutionInquiryResponse;
/** 기관 body code를 CPF 공통 Boundary Outcome으로 변환합니다. */
public final class InstitutionOutcomeAdapter {
    public CpfResult<InstitutionInquiryResponse> classify(CpfResult<InstitutionInquiryResponse> transportResult) {
        if (!transportResult.isSuccess()) return transportResult;
        InstitutionInquiryResponse response = transportResult.requireData();
        if ("0000".equals(response.resultCode())) return transportResult;
        if (response.resultCode() != null && response.resultCode().startsWith("B")) {
            return CpfResult.businessFailure(response.resultCode(), "기관 업무 규칙에 의해 거부되었습니다.");
        }
        return CpfResult.technicalFailure(response.resultCode(), "기관 응답 계약을 해석할 수 없습니다.");
    }
}
