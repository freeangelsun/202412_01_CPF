package fps.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * FPS 프레임워크 표준 오류 코드입니다.
 *
 * <p>개발자는 임의의 문자열 오류코드를 만들지 않고 이 enum을 우선 사용합니다.
 * 업무별 세부 코드가 필요하면 이 enum을 확장하거나, 같은 코드 체계를 따르는 별도 enum을 만든 뒤
 * {@link FpsException}에 메시지 키와 내부 메시지를 명시합니다.</p>
 */
public enum FpsErrorCode implements FpsErrorDefinition {
    INVALID_PARAMETER("2001", "ERR.INVALID_PARAMETER", HttpStatus.BAD_REQUEST,
            "요청 값이 올바르지 않습니다.", "요청 파라미터 검증에 실패했습니다."),
    NOT_FOUND("2002", "ERR.NOT_FOUND", HttpStatus.NOT_FOUND,
            "요청한 정보를 찾을 수 없습니다.", "조회 대상 데이터가 존재하지 않습니다."),
    DUPLICATE("2003", "ERR.DUPLICATE", HttpStatus.CONFLICT,
            "이미 등록된 정보입니다.", "중복 데이터가 감지되었습니다."),
    VALIDATION_FAILED("2004", "ERR.VALIDATION_FAILED", HttpStatus.BAD_REQUEST,
            "입력값을 확인해 주세요.", "Bean Validation 검증에 실패했습니다."),
    UNAUTHORIZED("2005", "ERR.UNAUTHORIZED", HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다.", "인증되지 않은 요청입니다."),
    FORBIDDEN("2006", "ERR.FORBIDDEN", HttpStatus.FORBIDDEN,
            "처리 권한이 없습니다.", "인가되지 않은 요청입니다."),
    BUSINESS_RULE_VIOLATION("2100", "ERR.BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST,
            "요청을 처리할 수 없습니다.", "업무 규칙 위반이 발생했습니다."),
    EXTERNAL_SERVICE_ERROR("3002", "ERR.EXTERNAL_SERVICE_ERROR", HttpStatus.BAD_GATEWAY,
            "일시적으로 처리할 수 없습니다.", "외부 또는 타 주제영역 연계 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR("3000", "ERR.INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR("3001", "ERR.DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "데이터베이스 처리 오류가 발생했습니다.");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    FpsErrorCode(
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

    public String getStatusCode() {
        return statusCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getExternalMessageKey() {
        return messageCode + ".EXTERNAL";
    }

    public String getInternalMessageKey() {
        return messageCode + ".INTERNAL";
    }

    public String getDefaultExternalMessage() {
        return defaultExternalMessage;
    }

    public String getDefaultInternalMessage() {
        return defaultInternalMessage;
    }
}
