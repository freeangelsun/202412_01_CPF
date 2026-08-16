package com.cpf.data.persistence.api;

import com.cpf.data.persistence.api.CpfRepositoryPort;
import java.util.Collection;

/** 대량 변경이 필요한 Domain이 명시적으로 선택하는 bounded bulk 계약입니다. */
public interface CpfBulkRepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
    int insertAll(Collection<T> values);
    int updateAll(Collection<T> values);
    int deleteAllById(Collection<ID> ids);
}
