package fps.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * PFW 프레임워크 코어에서 발생시키는 오류코드입니다.
 *
 * <p>업무 오류는 {@link FpsErrorCode} 또는 {@link FpsDynamicErrorCode}를 사용하고,
 * 거래 헤더 검증, 거래 메타데이터 검증, 프레임워크 내부 처리 실패처럼 PFW 자체에서 발생한 오류는
 * 이 enum의 {@code PFW.*} 코드로 관리합니다.</p>
 */
public enum FpsFrameworkErrorCode implements FpsErrorDefinition {
    MISSING_TRANSACTION_HEADER("9001", "PFW.MISSING_TRANSACTION_HEADER", HttpStatus.BAD_REQUEST,
            "필수 거래 헤더가 누락되었습니다.", "PFW 거래 헤더 검증에 실패했습니다."),
    INVALID_TRANSACTION_METADATA("9002", "PFW.INVALID_TRANSACTION_METADATA", HttpStatus.INTERNAL_SERVER_ERROR,
            "거래 메타데이터 설정이 올바르지 않습니다.", "PFW @FpsTransaction 메타데이터 검증에 실패했습니다."),
    SERVICE_ENDPOINT_NOT_FOUND("9003", "PFW.SERVICE_ENDPOINT_NOT_FOUND", HttpStatus.INTERNAL_SERVER_ERROR,
            "서비스 접속 정보가 없습니다.", "PFW 서비스 엔드포인트 설정을 찾을 수 없습니다."),
    DYNAMIC_LOG_RULE_INVALID("9004", "PFW.DYNAMIC_LOG_RULE_INVALID", HttpStatus.BAD_REQUEST,
            "동적 로그레벨 설정 요청이 올바르지 않습니다.", "PFW 동적 로그레벨 규칙 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR("9900", "PFW.INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "PFW 내부 오류가 발생했습니다.");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    FpsFrameworkErrorCode(
            String statusCode,
            String messageCode,
            HttpStatus httpStatus,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.httpStatus = httpStatus;
        this.defaultExternalMessage = defaultExternalMessage;
        this.defaultInternalMessage = defaultInternalMessage;
    }

    @Override
    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessageCode() {
        return messageCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getDefaultExternalMessage() {
        return defaultExternalMessage;
    }

    @Override
    public String getDefaultInternalMessage() {
        return defaultInternalMessage;
    }
}
