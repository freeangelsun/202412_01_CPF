package com.cpf.core.api.base;

import org.springframework.http.ResponseEntity;

/**
 * Generated Domain과 고객 업무 Controller가 사용할 수 있는 CPF 공개 기반 Controller입니다.
 *
 * <p>Runtime 구현 package를 노출하지 않고 표준 HTTP 응답 편의만 제공합니다.</p>
 */
public abstract class CpfBaseController {
    protected final <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
}
