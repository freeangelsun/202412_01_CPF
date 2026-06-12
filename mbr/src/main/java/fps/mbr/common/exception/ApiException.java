package fps.mbr.common.exception;

import fps.mbr.common.response.ResponseCode;
import lombok.Getter;

/**
 * 금융권 API 예외 정의
 * - 모든 비즈니스 로직 예외는 이 클래스를 상속받아 정의
 * - 응답 코드와 메시지를 함께 관리
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Getter
public class ApiException extends RuntimeException {
    
    /** 응답 코드 */
    private final ResponseCode responseCode;
    
    /** 에러 메시지 */
    private final String errorMessage;
    
    /** 에러 상세 정보 */
    private final String details;
    
    /**
     * 생성자 1: 응답 코드만 사용
     * @param responseCode 응답 코드
     */
    public ApiException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.errorMessage = responseCode.getMessage();
        this.details = null;
    }
    
    /**
     * 생성자 2: 응답 코드와 커스텀 메시지
     * @param responseCode 응답 코드
     * @param errorMessage 커스텀 에러 메시지
     */
    public ApiException(ResponseCode responseCode, String errorMessage) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = null;
    }
    
    /**
     * 생성자 3: 응답 코드, 메시지, 상세 정보
     * @param responseCode 응답 코드
     * @param errorMessage 에러 메시지
     * @param details 상세 정보
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
    
    /**
     * 생성자 4: 응답 코드, 메시지, 상세 정보, Cause
     * @param responseCode 응답 코드
     * @param errorMessage 에러 메시지
     * @param details 상세 정보
     * @param cause 원인 예외
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details, Throwable cause) {
        super(errorMessage, cause);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}
