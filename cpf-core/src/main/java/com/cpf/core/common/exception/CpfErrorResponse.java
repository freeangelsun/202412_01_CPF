package com.cpf.core.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.cpf.core.common.logging.TransactionContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** CPF 공통 오류 응답 본문입니다. 내부 예외 상세는 포함하지 않습니다. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CpfErrorResponse {
    private String messageId;
    private String transactionId;
    private String traceId;
    private String statusCode;
    private String messageCode;
    private String message;
    private String messageContent;
    private ErrorDetail errorDetail;
    private LocalDateTime timestamp;

    public static CpfErrorResponse of(
            CpfErrorDefinition errorCode,
            String externalMessage,
            String errorType,
            String fieldName) {
        return of(
                new CpfResolvedResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getStatusCode(),
                        errorCode.getMessageCode(),
                        externalMessage,
                        errorCode.getDefaultInternalMessage(),
                        errorCode.getStatusCode(),
                        errorCode.getDefaultInternalMessage()),
                externalMessage,
                errorType,
                fieldName);
    }

    public static CpfErrorResponse of(
            CpfResolvedResponse resolvedResponse,
            String externalMessage,
            String errorType,
            String fieldName) {
        return CpfErrorResponse.builder()
                .messageId("MSG_" + System.currentTimeMillis())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(resolvedResponse.responseCode())
                .messageCode(resolvedResponse.messageCode())
                .message(externalMessage)
                .messageContent(externalMessage)
                .errorDetail(ErrorDetail.builder()
                        .errorType(errorType)
                        .fieldName(fieldName)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String errorType;
        private String fieldName;
    }
}
