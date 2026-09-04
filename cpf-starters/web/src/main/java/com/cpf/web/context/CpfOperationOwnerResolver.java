package com.cpf.web.context;

import org.springframework.web.method.HandlerMethod;

/** Resolves the explicitly declared canonical owner of a business HTTP operation. */
@FunctionalInterface
public interface CpfOperationOwnerResolver {
    /**
     * Returns {@code null} only when this resolver has no explicit descriptor for the handler.
     * Callers must fail closed when a topology hosts a business operation with no resolvable owner.
     */
    CpfOperationOwner resolve(HandlerMethod handlerMethod, String operationId);

    record CpfOperationOwner(String systemCode, String domainCode, String application, String scanPackage) {}
}
