package cpf.mbr.common.exception;

import cpf.mbr.common.response.ResponseCode;
import lombok.Getter;

/**
 * MBR API 처리 중 발생한 업무 오류를 표현하는 예외입니다.
 * 공통 응답 코드, 사용자 메시지, 상세 원인을 함께 보관해 전역 예외 처리기에서 표준 응답으로 변환합니다.
 */
@Getter
public class ApiException extends RuntimeException {

    /** 표준 응답 코드입니다. */
    private final ResponseCode responseCode;

    /** 외부 응답에 사용할 오류 메시지입니다. */
    private final String errorMessage;

    /** 운영 진단에 참고할 상세 설명입니다. */
    private final String details;

    /**
     * 응답 코드에 등록된 기본 메시지로 예외를 생성합니다.
     *
     * @param responseCode 표준 응답 코드
     */
    public ApiException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.errorMessage = responseCode.getMessage();
        this.details = null;
    }

    /**
     * 호출자가 지정한 메시지로 예외를 생성합니다.
     *
     * @param responseCode 표준 응답 코드
     * @param errorMessage 외부 응답에 사용할 오류 메시지
     */
    public ApiException(ResponseCode responseCode, String errorMessage) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = null;
    }

    /**
     * 오류 메시지와 상세 설명을 함께 담아 예외를 생성합니다.
     *
     * @param responseCode 표준 응답 코드
     * @param errorMessage 외부 응답에 사용할 오류 메시지
     * @param details 운영 진단에 참고할 상세 설명
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }

    /**
     * 원인 예외까지 포함해 예외를 생성합니다.
     *
     * @param responseCode 표준 응답 코드
     * @param errorMessage 외부 응답에 사용할 오류 메시지
     * @param details 운영 진단에 참고할 상세 설명
     * @param cause 원인 예외
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details, Throwable cause) {
        super(errorMessage, cause);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}
