package fps.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 업무 개발자가 enum 추가 없이 런타임에 조립해서 사용할 수 있는 오류정의입니다.
 *
 * <p>대표 오류코드는 재사용하고 메시지 키와 메시지 인자만 달리 쓰면,
 * 오류코드가 업무 케이스별로 불필요하게 늘어나는 문제를 줄일 수 있습니다.</p>
 */
public class FpsDynamicErrorCode implements FpsErrorDefinition {
    private final String statusCode;
    private final String messageCode;
    private final String messageKeyPrefix;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    public FpsDynamicErrorCode(
            String statusCode,
            String messageCode,
            String messageKeyPrefix,
            HttpStatus httpStatus,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.messageKeyPrefix = hasText(messageKeyPrefix) ? messageKeyPrefix : messageCode;
        this.httpStatus = httpStatus;
        this.defaultExternalMessage = defaultExternalMessage;
        this.defaultInternalMessage = defaultInternalMessage;
    }

    /**
     * 업무 규칙 위반 계열의 동적 오류정의를 생성합니다.
     *
     * @param messageKeyPrefix 메시지 테이블 키 prefix. 예: {@code ERR.DUPLICATE}
     * @param defaultExternalMessage 메시지 테이블 값이 없을 때 쓸 고객용 기본 메시지
     * @param defaultInternalMessage 메시지 테이블 값이 없을 때 쓸 내부 기본 메시지
     * @return 업무 규칙 위반 동적 오류정의
     */
    public static FpsDynamicErrorCode business(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new FpsDynamicErrorCode(
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getStatusCode(),
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getMessageCode(),
                messageKeyPrefix,
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getHttpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
    }

    /**
     * 중복 데이터 계열의 동적 오류정의를 생성합니다.
     *
     * @param messageKeyPrefix 메시지 테이블 키 prefix. 예: {@code ERR.DUPLICATE}
     * @param defaultExternalMessage 메시지 테이블 값이 없을 때 쓸 고객용 기본 메시지
     * @param defaultInternalMessage 메시지 테이블 값이 없을 때 쓸 내부 기본 메시지
     * @return 중복 데이터 동적 오류정의
     */
    public static FpsDynamicErrorCode duplicate(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new FpsDynamicErrorCode(
                FpsErrorCode.DUPLICATE.getStatusCode(),
                FpsErrorCode.DUPLICATE.getMessageCode(),
                messageKeyPrefix,
                FpsErrorCode.DUPLICATE.getHttpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
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

    @Override
    public String getExternalMessageKey() {
        return messageKeyPrefix + ".EXTERNAL";
    }

    @Override
    public String getInternalMessageKey() {
        return messageKeyPrefix + ".INTERNAL";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
