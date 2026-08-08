package com.cpf.core.api.transaction;

import java.util.Objects;
import javax.transaction.xa.XAResource;

/** Provider 중립적인 이름과 표준 XAResource를 결합한 enlist 단위입니다. */
public record CpfXaResourceHandle(String resourceId, XAResource resource) {
    public CpfXaResourceHandle {
        if (resourceId == null || resourceId.isBlank()) throw new IllegalArgumentException("resourceId must not be blank");
        Objects.requireNonNull(resource, "resource");
    }
}
