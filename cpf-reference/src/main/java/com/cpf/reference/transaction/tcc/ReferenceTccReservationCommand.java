package com.cpf.reference.transaction.tcc;

import java.math.BigDecimal;

/** Reference business hold command. The framework never invents this business meaning. */
public record ReferenceTccReservationCommand(String accountId, BigDecimal amount) {
    public ReferenceTccReservationCommand {
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("accountId is required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
