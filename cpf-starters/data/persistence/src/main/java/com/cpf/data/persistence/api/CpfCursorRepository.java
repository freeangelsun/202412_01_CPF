package com.cpf.data.persistence.api;
import com.cpf.data.persistence.api.page.CpfCursor;
import com.cpf.data.persistence.api.page.CpfCursorPage;
import com.cpf.data.persistence.api.page.CpfSort;
import java.util.List;
/** Cursor/Keyset pagination CPF extension입니다. */
public interface CpfCursorRepository<T,Q>{ CpfCursorPage<T> findAfter(Q criteria,CpfCursor after,int size,List<CpfSort> sorts); }
