package cpf.mbr.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import cpf.pfw.common.logging.TransactionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/** MBR API가 사용하는 표준 응답 본문입니다. */
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

    /** SMBR000000 또는 EMBR010002 형식의 응답 코드입니다. */
    private String statusCode;

    /** MMBR000000 또는 MMBR010102 형식의 메시지 코드입니다. */
    private String messageCode;

    /** 기존 클라이언트 호환을 위한 메시지 필드입니다. */
    private String message;

    /** 공통 응답 표준이 명시적으로 제공하는 메시지 내용입니다. */
    private String messageContent;

    private T data;
    private ErrorDetail errorDetail;
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> ok(ResponseCode code, T data) {
        BaseResponse<T> response = BaseResponse.base(code, code.getMessage());
        response.data = data;
        return response;
    }

    public static <T> BaseResponse<T> ok(ResponseCode code) {
        return BaseResponse.base(code, code.getMessage());
    }

    public static <T> BaseResponse<T> error(ResponseCode code, String errorMessage) {
        String resolvedMessage = errorMessage != null ? errorMessage : code.getMessage();
        return BaseResponse.base(code, resolvedMessage);
    }

    public static <T> BaseResponse<T> error(ResponseCode code, ErrorDetail errorDetail) {
        BaseResponse<T> response = BaseResponse.base(code, code.getMessage());
        response.errorDetail = errorDetail;
        return response;
    }

    private static <T> BaseResponse<T> base(ResponseCode code, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.messageId = generateMessageId();
        response.transactionId = TransactionContext.getOrCreateTransactionId();
        response.traceId = TransactionContext.getOrCreateTraceId();
        response.statusCode = code.getCode();
        response.messageCode = code.getMessageCode();
        response.message = message;
        response.messageContent = message;
        response.timestamp = LocalDateTime.now();
        return response;
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

