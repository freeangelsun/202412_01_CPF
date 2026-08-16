package com.cpf.education.transaction.recovery.error;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.data.error.CpfPersistenceErrorMapper;
import com.cpf.integration.error.CpfExternalErrorMapper;
import com.cpf.messaging.error.CpfMessageErrorMapper;
import com.cpf.web.error.CpfHttpErrorMapper;
import java.sql.SQLTransientException;

/**
 * Core 기술 중립 Error semantics가 각 Runtime Owner에서 실제 의미로 매핑되는 Golden Path 예제입니다.
 * HTTP/SQL/Broker 세부 상태를 Core 예외에 저장하지 않습니다.
 */
public final class CpfErrorSemanticsEducationExamples {
    private CpfErrorSemanticsEducationExamples() { }

    /** Web Owner가 Core category를 HTTP 상태로 변환합니다. */
    public static int httpStatus() {
        return CpfHttpErrorMapper.status(CpfErrorCode.NOT_FOUND).value();
    }

    /** Data Owner가 Provider 예외를 Persistence 분류로 변환합니다. */
    public static String persistenceFailure() {
        return CpfPersistenceErrorMapper.classify(new SQLTransientException("temporary")).name();
    }

    /** Integration Owner가 retry/reconcile semantics를 외부 연계 정책으로 변환합니다. */
    public static String externalDisposition() {
        return CpfExternalErrorMapper.disposition(CpfErrorCode.EXTERNAL_UNKNOWN_OUTCOME.retryDisposition()).name();
    }

    /** Messaging Owner가 retry/DLQ/reconcile 의미를 결정합니다. */
    public static String messageFailureAction() {
        return CpfMessageErrorMapper.action(CpfErrorCode.EXTERNAL_UNKNOWN_OUTCOME).name();
    }
}
