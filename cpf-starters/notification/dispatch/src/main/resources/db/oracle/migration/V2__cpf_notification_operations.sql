CREATE TABLE cpf_notification_operation_audit (
 operation_id VARCHAR2(64) NOT NULL,
 notification_id VARCHAR2(64) NOT NULL,
 operation_code VARCHAR2(64) NOT NULL,
 operator_id VARCHAR2(128) NOT NULL,
 operation_reason VARCHAR2(1000) NOT NULL,
 before_status VARCHAR2(32) NOT NULL,
 after_status VARCHAR2(32) NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
 CONSTRAINT pk_cpf_notification_operation PRIMARY KEY(operation_id)
);
CREATE INDEX ix_cpf_notification_operation_notification ON cpf_notification_operation_audit(notification_id,created_at);
CREATE TABLE cpf_notification_receipt (
 receipt_id VARCHAR2(128) NOT NULL,
 notification_id VARCHAR2(64) NOT NULL,
 provider_name VARCHAR2(128) NOT NULL,
 receipt_status VARCHAR2(32) NOT NULL,
 receipt_detail VARCHAR2(2000),
 received_at TIMESTAMP NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
 CONSTRAINT pk_cpf_notification_receipt PRIMARY KEY(receipt_id)
);
CREATE INDEX ix_cpf_notification_receipt_notification ON cpf_notification_receipt(notification_id,received_at);
