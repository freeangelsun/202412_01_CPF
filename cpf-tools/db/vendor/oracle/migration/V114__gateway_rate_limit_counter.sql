CREATE TABLE GW_RATE_LIMIT_COUNTER (
  counter_key VARCHAR2(300) NOT NULL,
  policy_version NUMBER(19) NOT NULL,
  window_start_ms NUMBER(19) NOT NULL,
  reset_at_ms NUMBER(19) NOT NULL,
  used_units NUMBER(19) DEFAULT 0 NOT NULL,
  rejected_count NUMBER(10) DEFAULT 0 NOT NULL,
  blocked_until_ms NUMBER(19) DEFAULT 0 NOT NULL,
  version NUMBER(19) DEFAULT 0 NOT NULL,
  CONSTRAINT pk_gw_rate_limit_counter PRIMARY KEY (counter_key, policy_version, window_start_ms)
);
CREATE INDEX ix_gw_rate_limit_active_block ON GW_RATE_LIMIT_COUNTER(counter_key, policy_version, blocked_until_ms);
CREATE INDEX ix_gw_rate_limit_reset ON GW_RATE_LIMIT_COUNTER(reset_at_ms);
CREATE TABLE GW_RATE_LIMIT_REQUEST (
  counter_key VARCHAR2(300) NOT NULL,
  policy_version NUMBER(19) NOT NULL,
  window_start_ms NUMBER(19) NOT NULL,
  request_id VARCHAR2(200) NOT NULL,
  request_hash VARCHAR2(64) NOT NULL,
  accepted NUMBER(10) NOT NULL,
  used_units NUMBER(19) NOT NULL,
  remaining_units NUMBER(19) NOT NULL,
  reset_at_ms NUMBER(19) NOT NULL,
  blocked_until_ms NUMBER(19) DEFAULT 0 NOT NULL,
  rejected_count NUMBER(10) DEFAULT 0 NOT NULL,
  reason VARCHAR2(200) NOT NULL,
  limiting_index NUMBER(10) DEFAULT -1 NOT NULL,
  CONSTRAINT pk_gw_rate_limit_request PRIMARY KEY (counter_key, policy_version, window_start_ms, request_id),
  CONSTRAINT fk_gw_rate_limit_request_counter FOREIGN KEY (counter_key, policy_version, window_start_ms)
    REFERENCES GW_RATE_LIMIT_COUNTER(counter_key, policy_version, window_start_ms),
  CONSTRAINT ck_gw_rate_limit_request_accepted CHECK (accepted IN (0,1))
);
CREATE INDEX ix_gw_rate_limit_request_id ON GW_RATE_LIMIT_REQUEST(request_id);
