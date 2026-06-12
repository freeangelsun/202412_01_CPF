package fps.mbr.common.response;

/**
 * 금융권 표준 응답 코드 정의
 * 모든 API 응답에서 사용되는 통일된 응답 상태 코드
 * - 1000번대: 정상 (SUCCESS)
 * - 2000번대: 클라이언트 오류 (CLIENT_ERROR)
 * - 3000번대: 서버 오류 (SERVER_ERROR)
 * 
 * @author FPS Team
 * @version 1.0.0
 */
public enum ResponseCode {
    // 정상 응답 (1000번대)
    SUCCESS("1000", "성공"),
    CREATED("1001", "생성 성공"),
    UPDATED("1002", "수정 성공"),
    DELETED("1003", "삭제 성공"),
    
    // 클라이언트 오류 (2000번대)
    BAD_REQUEST("2000", "잘못된 요청"),
    INVALID_PARAMETER("2001", "유효하지 않은 파라미터"),
    NOT_FOUND("2002", "요청한 자원을 찾을 수 없음"),
    DUPLICATE("2003", "중복된 데이터"),
    VALIDATION_FAILED("2004", "입력값 검증 실패"),
    UNAUTHORIZED("2005", "인증되지 않은 요청"),
    FORBIDDEN("2006", "접근 권한 없음"),
    
    // 서버 오류 (3000번대)
    INTERNAL_SERVER_ERROR("3000", "내부 서버 오류"),
    DATABASE_ERROR("3001", "데이터베이스 오류"),
    EXTERNAL_SERVICE_ERROR("3002", "외부 서비스 오류");
    
    private final String code;
    private final String message;
    
    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
