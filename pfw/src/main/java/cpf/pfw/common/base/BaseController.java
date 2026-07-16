package cpf.pfw.common.base;

import org.springframework.http.ResponseEntity;

/**
 * CPF Controller가 공통 응답 계약을 일관되게 사용하도록 제공하는 최소 확장점입니다.
 *
 * <p>업무 Controller의 상속 깊이를 늘리지 않도록 상태를 갖지 않으며, 인증·감사·거래 로깅은
 * PFW filter와 aspect가 처리합니다. 업무 모듈은 필요할 때 이 형식을 상속해 응답 생성만 재사용합니다.</p>
 */
public abstract class BaseController {

    protected final <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
}
