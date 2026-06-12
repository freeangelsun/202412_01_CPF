package fps.pfw.common.exception;

import java.util.Locale;

/**
 * PFW 예외 응답에 사용할 메시지를 조회하는 확장 인터페이스입니다.
 *
 * <p>PFW는 CMN을 직접 의존하지 않습니다. CMN 또는 업무 모듈이 이 인터페이스 구현체를 Bean으로 등록하면
 * 프레임워크 예외 핸들러가 코드/메시지 테이블의 캐시 값을 자동 사용합니다.</p>
 */
public interface FpsMessageResolver {

    /**
     * 고객용/내부용 오류 메시지를 조회합니다.
     *
     * @param errorCode 표준 오류 정의
     * @param locale 요청 언어
     * @return 해석된 고객용/내부용 메시지
     */
    FpsResolvedMessage resolve(FpsErrorDefinition errorCode, Locale locale);
}
