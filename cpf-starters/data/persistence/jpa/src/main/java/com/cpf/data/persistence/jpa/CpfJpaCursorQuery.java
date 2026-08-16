package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.page.CpfCursor;
import com.cpf.data.persistence.api.page.CpfCursorPage;
import com.cpf.data.persistence.api.page.CpfSort;
import com.cpf.data.persistence.api.CpfSearchSpec;
import java.util.List;

/** Entity별 keyset 의미론을 명시적으로 구현하는 JPA Cursor escape/extension contract입니다. */
@FunctionalInterface
public interface CpfJpaCursorQuery<T> {
    CpfCursorPage<T> find(CpfSearchSpec filter, CpfCursor cursor, int size, List<CpfSort> sorts);
}
