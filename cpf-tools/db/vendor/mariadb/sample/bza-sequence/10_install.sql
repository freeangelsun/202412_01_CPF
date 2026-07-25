-- Optional BZA sequence customization sample. NOT part of default install.
CREATE TABLE IF NOT EXISTS bza_sequence_sample_rule (
 rule_code VARCHAR(50) PRIMARY KEY, rule_name VARCHAR(100) NOT NULL, prefix VARCHAR(40) NULL,
 current_value BIGINT NOT NULL DEFAULT 0, padding_length INT NOT NULL DEFAULT 8, use_yn CHAR(1) NOT NULL DEFAULT 'Y',
 created_by VARCHAR(100) NOT NULL, updated_by VARCHAR(100) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE TABLE IF NOT EXISTS bza_sequence_sample_issue (
 issue_id VARCHAR(36) PRIMARY KEY, rule_code VARCHAR(50) NOT NULL, issued_value VARCHAR(100) NOT NULL,
 operator_id VARCHAR(100) NOT NULL, reason VARCHAR(500) NOT NULL, issued_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_bza_sequence_sample_issue (rule_code, issued_at),
 CONSTRAINT fk_bza_sequence_sample_issue_rule FOREIGN KEY (rule_code) REFERENCES bza_sequence_sample_rule(rule_code)
);
