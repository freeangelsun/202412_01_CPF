package com.cpf.data.persistence.api;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfValidationException;

/** Interface 기반 Repository가 공유하는 최소 CPF Persistence Contract입니다. */
public interface CpfRepositoryContract<ID> {
    default CpfContextSnapshot requireRepositoryContext() { return CpfContexts.requireSnapshot(); }
    default ID requireRepositoryId(ID id) {
        if (id == null) throw new CpfValidationException("repository id is required");
        return id;
    }
}
