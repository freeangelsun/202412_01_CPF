package com.cpf.education.integration.counterparty.persistence;
import com.cpf.education.integration.counterparty.model.EducationCounterpartyExchange;
import java.util.Optional;

/** Product port. Implementations must enforce requirementId+idempotencyKey uniqueness. */
/** EducationCounterpartyStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface EducationCounterpartyStore {
    Optional<EducationCounterpartyExchange> find(String requirementId, String idempotencyKey);
    boolean insert(EducationCounterpartyExchange exchange);
    void update(EducationCounterpartyExchange exchange);
}
