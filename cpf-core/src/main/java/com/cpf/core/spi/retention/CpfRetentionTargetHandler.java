package com.cpf.core.spi.retention;

import com.cpf.core.api.retention.CpfRetentionCommand;
import com.cpf.core.api.retention.CpfRetentionResult;

/** Owner Module이 실제 보존/Archive/Purge를 구현하는 확장 SPI. */
public interface CpfRetentionTargetHandler {
    String target();
    CpfRetentionResult execute(CpfRetentionCommand command);
}
