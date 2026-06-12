package fps.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * FPS 오류코드가 반드시 제공해야 하는 표준 계약입니다.
 *
 * <p>업무 오류코드를 enum에 계속 추가하면 코드가 기하급수적으로 늘어날 수 있습니다.
 * 그래서 PFW는 이 인터페이스를 기준으로 enum 오류코드, 프레임워크 코어 오류코드,
 * 업무에서 런타임에 조립하는 동적 오류코드를 모두 같은 방식으로 처리합니다.</p>
 */
public interface FpsErrorDefinition {

    /**
     * 응답과 로그에 남길 프레임워크 상태 코드입니다.
     *
     * @return 상태 코드
     */
    String getStatusCode();

    /**
     * 응답 헤더, 응답 바디, 거래 로그에 남길 대표 오류코드입니다.
     *
     * @return 대표 오류코드
     */
    String getMessageCode();

    /**
     * HTTP 응답 상태입니다.
     *
     * @return HTTP 상태
     */
    HttpStatus getHttpStatus();

    /**
     * 메시지 테이블에 값이 없을 때 사용할 고객용 기본 메시지입니다.
     *
     * @return 고객용 기본 메시지
     */
    String getDefaultExternalMessage();

    /**
     * 메시지 테이블에 값이 없을 때 사용할 내부 추적용 기본 메시지입니다.
     *
     * @return 내부 기본 메시지
     */
    String getDefaultInternalMessage();

    /**
     * 고객용 메시지 테이블 키입니다.
     *
     * @return EXTERNAL 메시지 키
     */
    default String getExternalMessageKey() {
        return getMessageCode() + ".EXTERNAL";
    }

    /**
     * 내부용 메시지 테이블 키입니다.
     *
     * @return INTERNAL 메시지 키
     */
    default String getInternalMessageKey() {
        return getMessageCode() + ".INTERNAL";
    }
}
