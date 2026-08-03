CREATE INDEX idx_cpf_broker_outbox_reconcile ON cpf_broker_outbox (outbox_status, next_attempt_at, lease_until);
