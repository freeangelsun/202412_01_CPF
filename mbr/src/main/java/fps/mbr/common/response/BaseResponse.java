package fps.mbr.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fps.pfw.common.logging.TransactionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 금융권 표준 응답 객체
 * 모든 API 응답은 이 객체를 통해 반환
 * 응답에는 항상 messageId(추적), traceId(감시), 상태코드, 메시지, 데이터 포함
 * 
 * @param <T> 응답 데이터 타입
 * @author FPS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 메시지 ID (요청 추적용) */
    private String messageId;

    /** 거래 ID (업무 거래 전체 추적용) */
    private String transactionId;
    
    /** 트레이스 ID (로그 추적용) */
    private String traceId;
    
    /** 응답 상태 코드 */
    private String statusCode;
    
    /** 응답 메시지 */
    private String message;
    
    /** 응답 데이터 (제네릭) */
    private T data;
    
    /** 에러 상세 정보 */
    private ErrorDetail errorDetail;
    
    /** 응답 타임스탬프 */
    private LocalDateTime timestamp;
    
    /**
     * 성공 응답 빌더
     * @param code 응답 코드
     * @param data 응답 데이터
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> ok(ResponseCode code, T data) {
        return BaseResponse.<T>builder()
                .messageId(generateMessageId())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(code.getCode())
                .message(code.getMessage())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 성공 응답 빌더 (데이터 없음)
     * @param code 응답 코드
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> ok(ResponseCode code) {
        return BaseResponse.<T>builder()
                .messageId(generateMessageId())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(code.getCode())
                .message(code.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 빌더
     * @param code 응답 코드
     * @param errorMessage 에러 메시지
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ResponseCode code, String errorMessage) {
        return BaseResponse.<T>builder()
                .messageId(generateMessageId())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(code.getCode())
                .message(errorMessage != null ? errorMessage : code.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 빌더 (상세 정보 포함)
     * @param code 응답 코드
     * @param errorDetail 에러 상세 정보
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ResponseCode code, ErrorDetail errorDetail) {
        return BaseResponse.<T>builder()
                .messageId(generateMessageId())
                .transactionId(TransactionContext.getOrCreateTransactionId())
                .traceId(TransactionContext.getOrCreateTraceId())
                .statusCode(code.getCode())
                .message(code.getMessage())
                .errorDetail(errorDetail)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 메시지 ID 생성 (타임스탬프 기반)
     * 형식: MSG_YYYYMMDDHHmmss_RANDOM
     */
    private static String generateMessageId() {
        return "MSG_" + System.currentTimeMillis();
    }
    
    /**
     * 에러 상세 정보 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        /** 에러 타입 */
        private String errorType;
        
        /** 에러 필드 (입력값 검증 오류시 사용) */
        private String fieldName;
        
        /** 에러 상세 메시지 */
        private String details;
    }
}
