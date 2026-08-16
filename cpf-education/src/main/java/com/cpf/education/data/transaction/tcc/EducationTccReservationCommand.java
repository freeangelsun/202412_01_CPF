package com.cpf.education.data.transaction.tcc;
import java.math.BigDecimal;

/** Education business hold command. The framework never invents this business meaning. */
/** EducationTccReservationCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EducationTccReservationCommand(String accountId, BigDecimal amount) {
    public EducationTccReservationCommand {
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("accountId is required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
