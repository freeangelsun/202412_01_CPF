package cpf.pfw.common.base;

import org.springframework.http.ResponseEntity;

/**
 * CPF 웹 Controller가 사용하는 canonical 기술 확장점입니다.
 *
 * <p>상태나 Spring stereotype을 갖지 않으며 표준 응답 생성처럼 안정적인 기능만 제공합니다.
 * 인증, 감사, 마스킹과 거래 로깅은 PFW filter 및 aspect가 우회 불가능하게 처리합니다.</p>
 *
 * @since 1.0.0
 */
public abstract class CpfBaseController {

    /**
     * HTTP 200 응답을 생성합니다.
     *
     * @param body 응답 본문
     * @param <T> 응답 본문 형식
     * @return HTTP 200 응답
     */
    protected final <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
}
