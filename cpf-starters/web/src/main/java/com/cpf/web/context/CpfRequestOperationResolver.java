package com.cpf.web.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * Transport-specific request를 실제 Canonical operationId로 해석하는 Web 확장점입니다.
 *
 * <p>일반 업무 Controller는 {@link CpfOperationIdResolver}가 Annotation/OpenAPI 계약에서 해석합니다.
 * Generic transport처럼 URI의 요청 operationId와 실제 Registry operation이 분리되는 경우에만 이 SPI를
 * 구현합니다. 요청 문자열 자체를 권한/감사 정본으로 사용하지 않고, 실제 Registry를 먼저 resolve한 뒤
 * canonical operationId를 반환해야 합니다.</p>
 */
@FunctionalInterface
public interface CpfRequestOperationResolver {
    /**
     * @return 이 resolver가 처리하지 않는 요청이면 {@code null}, 처리하면 실제 등록된 canonical operationId
     */
    String resolve(HttpServletRequest request, HandlerMethod handlerMethod);
}
