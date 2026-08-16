package com.cpf.batch.execution.internal.context;

import com.cpf.batch.context.CpfBatchContextBundle;

/** Execution-Runtime 내부에서 Batch Owner metadata를 lexical scope로 유지합니다. */
public final class CpfBatchRuntimeContexts {
    private static final ThreadLocal<CpfBatchContextBundle> CURRENT=new ThreadLocal<>();
    private CpfBatchRuntimeContexts() {}
    public static CpfBatchContextBundle current(){return CURRENT.get();}
    public static AutoCloseable bind(CpfBatchContextBundle bundle){
        CpfBatchContextBundle previous=CURRENT.get(); CURRENT.set(bundle);
        return () -> { if(previous==null) CURRENT.remove(); else CURRENT.set(previous); };
    }
}
