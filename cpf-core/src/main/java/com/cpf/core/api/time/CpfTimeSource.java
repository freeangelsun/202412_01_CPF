package com.cpf.core.api.time;
import java.time.Instant;
public interface CpfTimeSource { Instant now(); long monotonicNanos(); }
