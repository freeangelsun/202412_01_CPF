package com.cpf.web.context;

/** Lexical access to normalized Web-only context. */
public final class CpfWebContexts {
    private static final ThreadLocal<CpfWebContext> CURRENT = new ThreadLocal<>();
    private CpfWebContexts() {}
    public static CpfWebContext current() { return CURRENT.get(); }
    public static Scope bind(CpfWebContext context) {
        CpfWebContext previous = CURRENT.get();
        if (context == null) CURRENT.remove(); else CURRENT.set(context);
        return () -> { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); };
    }
    @FunctionalInterface public interface Scope extends AutoCloseable { @Override void close(); }
}
