package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * PFW에서 공통으로 사용하는 표준 오류 코드입니다.
 */
public enum CpfErrorCode implements CpfErrorDefinition {
    INVALID_PARAMETER("EPFW010001", "MPFW010001", HttpStatus.BAD_REQUEST,
            "요청 값이 올바르지 않습니다.", "요청 파라미터 검증에 실패했습니다. field={0}, value={1}"),
    NOT_FOUND("EPFW010002", "MPFW010002", HttpStatus.NOT_FOUND,
            "요청한 정보를 찾을 수 없습니다.", "조회 대상 데이터가 존재하지 않습니다. target={0}"),
    DUPLICATE("EPFW010003", "MPFW010003", HttpStatus.CONFLICT,
            "이미 등록된 정보입니다.", "중복 데이터가 감지되었습니다. key={0}"),
    VALIDATION_FAILED("EPFW010004", "MPFW010004", HttpStatus.BAD_REQUEST,
            "입력값을 확인해 주세요.", "Bean Validation 검증에 실패했습니다. field={0}"),
    UNAUTHORIZED("EPFW010005", "MPFW010005", HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다.", "인증되지 않은 요청입니다."),
    FORBIDDEN("EPFW010006", "MPFW010006", HttpStatus.FORBIDDEN,
            "처리 권한이 없습니다.", "인가되지 않은 요청입니다. user={0}"),
    BUSINESS_RULE_VIOLATION("EPFW020001", "MPFW020001", HttpStatus.BAD_REQUEST,
            "요청을 처리할 수 없습니다.", "업무 규칙 위반이 발생했습니다. rule={0}"),
    EXTERNAL_SERVICE_ERROR("EPFW030001", "MPFW030001", HttpStatus.BAD_GATEWAY,
            "일시적으로 처리할 수 없습니다.", "외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}"),
    INTERNAL_SERVER_ERROR("EPFW990000", "MPFW990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "서버 내부 오류가 발생했습니다. error={0}"),
    DATABASE_ERROR("EPFW990001", "MPFW990001", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "데이터베이스 처리 오류가 발생했습니다. sqlState={0}");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    CpfErrorCode(
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
