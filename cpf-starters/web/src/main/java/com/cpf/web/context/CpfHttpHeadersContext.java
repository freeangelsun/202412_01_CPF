package com.cpf.web.context;

import com.cpf.web.api.CpfHttpHeaders;

/** Internal request-scope holder used by the public CpfHttpHeaders current-context facade. */
public final class CpfHttpHeadersContext {
    private static final ThreadLocal<CpfHttpHeaders> CURRENT = new ThreadLocal<>();
    private CpfHttpHeadersContext() {}
    public static CpfHttpHeaders current() { return CURRENT.get(); }
    public static Scope bind(CpfHttpHeaders headers) {
        CpfHttpHeaders previous = CURRENT.get();
        if (headers == null) CURRENT.remove(); else CURRENT.set(headers);
        return () -> { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); };
    }
    @FunctionalInterface public interface Scope extends AutoCloseable { @Override void close(); }
}
