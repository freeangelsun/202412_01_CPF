package com.cpf.backoffice.online.base;

import com.cpf.data.persistence.api.CpfBaseRepository;

/**
 * 운영 Persistence Repository의 Domain Base입니다.
 * Paging 상한과 statement 식별자 검증을 Framework Base 위에서 재사용합니다.
 */
public abstract class BackofficeBaseRepository extends CpfBaseRepository {
    protected static final int OPERATIONS_MAX_PAGE_SIZE = 200;

    protected final int operationPageSize(int requested) {
        return boundedSize(requested, 50, OPERATIONS_MAX_PAGE_SIZE);
    }
}
