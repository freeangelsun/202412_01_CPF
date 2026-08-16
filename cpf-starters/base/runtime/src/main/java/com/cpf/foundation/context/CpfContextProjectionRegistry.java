package com.cpf.foundation.context;
import com.cpf.core.api.context.CpfContextSnapshot;
import java.util.concurrent.CopyOnWriteArrayList;
/** Projection 실패가 업무 실행을 변경하지 않도록 best-effort fan-out 합니다. */
public final class CpfContextProjectionRegistry {
    private final CopyOnWriteArrayList<CpfContextProjection> listeners = new CopyOnWriteArrayList<>();
    public AutoCloseable register(CpfContextProjection projection) { listeners.addIfAbsent(projection); return () -> listeners.remove(projection); }
    public void project(CpfContextSnapshot snapshot) { for (var p : listeners) try { p.project(snapshot); } catch (RuntimeException ignored) { } }
    public void clear() { for (var p : listeners) try { p.clear(); } catch (RuntimeException ignored) { } }
    public int size() { return listeners.size(); }
}
