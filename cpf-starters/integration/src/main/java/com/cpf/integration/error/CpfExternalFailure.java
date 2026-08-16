package com.cpf.integration.error;
import com.cpf.core.api.error.CpfErrorCode;
/** Integration Owner의 외부 호출 실패 의미입니다. */
public record CpfExternalFailure(String target,boolean retryable,boolean unknownOutcome,String providerCode){ public CpfErrorCode errorCode(){ return CpfErrorCode.EXTERNAL_SERVICE_ERROR; } }
