package com.cpf.foundation.context;
import com.cpf.core.api.context.CpfContextSnapshot;
/** Logging/Tracing 등 Runtime projection hook입니다. Context 자체를 변경해서는 안 됩니다. */
public interface CpfContextProjection { void project(CpfContextSnapshot snapshot); void clear(); }
