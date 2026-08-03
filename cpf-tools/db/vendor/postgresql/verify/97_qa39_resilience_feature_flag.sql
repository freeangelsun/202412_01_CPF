-- CPF QA39 resilience and feature flag control plane. Generated from cpf-tools/db/canonical/platform-schema.json.
SELECT COUNT(*) AS cpf_resilience_policy_count FROM cpf_resilience_policy;
SELECT COUNT(*) AS cpf_resilience_policy_request_count FROM cpf_resilience_policy_request;
SELECT COUNT(*) AS cpf_resilience_audit_count FROM cpf_resilience_audit;
SELECT COUNT(*) AS cpf_feature_flag_override_request_count FROM cpf_feature_flag_override_request;
SELECT COUNT(*) AS cpf_feature_flag_override_count FROM cpf_feature_flag_override;
SELECT COUNT(*) AS cpf_feature_flag_kill_switch_count FROM cpf_feature_flag_kill_switch;
SELECT COUNT(*) AS cpf_feature_flag_revision_count FROM cpf_feature_flag_revision;
SELECT COUNT(*) AS cpf_feature_flag_audit_count FROM cpf_feature_flag_audit;
