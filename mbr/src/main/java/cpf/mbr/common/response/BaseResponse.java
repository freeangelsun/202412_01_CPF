package cpf.mbr.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import cpf.pfw.common.logging.TransactionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Standard API response body for MBR.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;
    private String transactionId;
    private String traceId;

    /** Response code such as SMBR000000 or EMBR010002. */
    private String statusCode;

    /** Message code such as MMBR000000 or MMBR010102. */
    private String messageCode;

    /** Backward compatible message field. */
    private String message;

    /** Explicit message content field for the common response standard. */
    private String messageContent;

    private T data;
    private ErrorDetail errorDetail;
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> ok(ResponseCode code, T data) {
        return BaseResponse.<T>base(code, code.getMessage())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> ok(ResponseCode code) {
        return BaseResponse.<T>base(code, code.getMessage()).build();
    }

    public static <T> BaseResponse<T> error(ResponseCode code, String errorMessage) {
        String resolvedMessage = errorMessage != null ? errorMessage : code.getMessage();
        return BaseResponse.<T>base(code, resolvedMessage).build();
    }

    public static <T> BaseResponse<T> error(ResponseCode code, ErrorDetail errorDetail) {
        return BaseResponse.<T>base(code, code.getMessage())
                .errorDetail(errorDetail)
                .build();
    }

    private static <T> BaseResponseBuilder<T> base(ResponseCode code, String message) {
        return BaseResponse.<T>builder()
                .messageId(generateMessageId())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(code.getCode())
                .messageCode(code.getMessageCode())
                .message(message)
                .messageContent(message)
                .timestamp(LocalDateTime.now());
    }

    private static String generateMessageId() {
        return "MSG_" + System.currentTimeMillis();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        private String errorType;
        private String fieldName;
        private String details;
    }
}

