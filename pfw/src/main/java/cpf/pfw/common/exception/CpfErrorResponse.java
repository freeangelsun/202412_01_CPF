package cpf.pfw.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import cpf.pfw.common.logging.TransactionContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CPF ?쒖? ?ㅻ쪟 ?묐떟?낅땲??
 *
 * <p>怨좉컼?먭쾶??{@code message}? {@code messageCode}留??대젮蹂대궡怨?
 * ?대? ?곸꽭 ?ъ쑀??DB 濡쒓렇? ?뚯씪 濡쒓렇???④퉩?덈떎.</p>
 */
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

