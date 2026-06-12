package fps.pfw.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import fps.pfw.common.logging.TransactionContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FPS 표준 오류 응답입니다.
 *
 * <p>고객에게는 {@code message}와 {@code messageCode}만 내려보내고,
 * 내부 상세 사유는 DB 로그와 파일 로그에 남깁니다.</p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FpsErrorResponse {
    private String messageId;
    private String transactionId;
    private String traceId;
    private String statusCode;
    private String messageCode;
    private String message;
    private ErrorDetail errorDetail;
    private LocalDateTime timestamp;

    public static FpsErrorResponse of(
            FpsErrorDefinition errorCode,
            String externalMessage,
            String errorType,
            String fieldName) {
        return FpsErrorResponse.builder()
                .messageId("MSG_" + System.currentTimeMillis())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(errorCode.getStatusCode())
                .messageCode(errorCode.getMessageCode())
                .message(externalMessage)
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
