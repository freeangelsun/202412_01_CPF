package com.cpf.core.api.retention;

import java.util.Set;

/** Owner별 Retention Handler를 호출하는 공개 Operations 계약. */
public interface CpfRetentionOperations {
    Set<String> targets();
    CpfRetentionResult execute(CpfRetentionCommand command);
}
