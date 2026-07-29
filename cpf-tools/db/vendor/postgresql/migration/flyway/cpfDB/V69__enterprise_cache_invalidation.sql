-- V69 CPF Enterprise Cache / Async File Job (PostgreSQL)
CREATE TABLE IF NOT EXISTS cpf_cache_invalidation_event (
    event_id BIGSERIAL PRIMARY KEY,
    event_key VARCHAR(100) NOT NULL UNIQUE,
    tenant_id VARCHAR(80) NOT NULL,
    namespace_cd VARCHAR(80) NOT NULL,
    cache_key VARCHAR(512) NOT NULL DEFAULT '',
    event_version BIGINT NOT NULL DEFAULT 0,
    reason VARCHAR(500) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_cpf_cache_inv_scope ON cpf_cache_invalidation_event(tenant_id, namespace_cd, event_id);

CREATE TABLE IF NOT EXISTS cpf_cache_invalidation_checkpoint (
    consumer_id VARCHAR(120) PRIMARY KEY,
    last_event_id BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
