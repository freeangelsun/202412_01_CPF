package com.cpf.integration.internal.context;

import com.cpf.integration.context.CpfIntegrationContext;
import java.util.ArrayDeque;

/** Integration Owner 내부 lexical Context 저장소입니다. */
public final class CpfIntegrationContextRuntime {
    private static final ThreadLocal<ArrayDeque<CpfIntegrationContext>> CURRENT=ThreadLocal.withInitial(ArrayDeque::new);
    private CpfIntegrationContextRuntime() { }
    public static CpfIntegrationContext current(){var stack=CURRENT.get();return stack.isEmpty()?null:stack.peek();}
    public static AutoCloseable bind(CpfIntegrationContext context){
        if(context==null) throw new IllegalArgumentException("context");
        var stack=CURRENT.get();stack.push(context);
        return new AutoCloseable(){
            private boolean closed;
            @Override public void close(){
                if(closed)return;closed=true;var values=CURRENT.get();
                if(values.isEmpty()||values.pop()!=context)throw new IllegalStateException("CPF integration context close order violated");
                if(values.isEmpty())CURRENT.remove();
            }
        };
    }
}
