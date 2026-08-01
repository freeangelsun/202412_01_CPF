package com.cpf.reference.edu.counterparty.persistence;

import com.cpf.reference.edu.counterparty.model.ReferenceCounterpartyExchange;
import java.util.Optional;

/** Product port. Implementations must enforce requirementId+idempotencyKey uniqueness. */
public interface ReferenceCounterpartyStore {
    Optional<ReferenceCounterpartyExchange> find(String requirementId, String idempotencyKey);
    boolean insert(ReferenceCounterpartyExchange exchange);
    void update(ReferenceCounterpartyExchange exchange);
}
