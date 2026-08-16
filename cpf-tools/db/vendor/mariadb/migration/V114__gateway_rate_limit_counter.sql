CREATE TABLE GW_RATE_LIMIT_COUNTER (
  counter_key VARCHAR(300) NOT NULL,
  policy_version BIGINT NOT NULL,
  window_start_ms BIGINT NOT NULL,
  reset_at_ms BIGINT NOT NULL,
  used_units BIGINT NOT NULL DEFAULT 0,
  rejected_count INT NOT NULL DEFAULT 0,
  blocked_until_ms BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT pk_gw_rate_limit_counter PRIMARY KEY (counter_key, policy_version, window_start_ms)
);
CREATE INDEX ix_gw_rate_limit_active_block ON GW_RATE_LIMIT_COUNTER(counter_key, policy_version, blocked_until_ms);
CREATE INDEX ix_gw_rate_limit_reset ON GW_RATE_LIMIT_COUNTER(reset_at_ms);
CREATE TABLE GW_RATE_LIMIT_REQUEST (
  counter_key VARCHAR(300) NOT NULL,
  policy_version BIGINT NOT NULL,
  window_start_ms BIGINT NOT NULL,
  request_id VARCHAR(200) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  accepted INT NOT NULL,
  used_units BIGINT NOT NULL,
  remaining_units BIGINT NOT NULL,
  reset_at_ms BIGINT NOT NULL,
  blocked_until_ms BIGINT NOT NULL DEFAULT 0,
  rejected_count INT NOT NULL DEFAULT 0,
  reason VARCHAR(200) NOT NULL,
  limiting_index INT NOT NULL DEFAULT -1,
  CONSTRAINT pk_gw_rate_limit_request PRIMARY KEY (counter_key, policy_version, window_start_ms, request_id),
  CONSTRAINT fk_gw_rate_limit_request_counter FOREIGN KEY (counter_key, policy_version, window_start_ms)
    REFERENCES GW_RATE_LIMIT_COUNTER(counter_key, policy_version, window_start_ms),
  CONSTRAINT ck_gw_rate_limit_request_accepted CHECK (accepted IN (0,1))
);
CREATE INDEX ix_gw_rate_limit_request_id ON GW_RATE_LIMIT_REQUEST(request_id);
