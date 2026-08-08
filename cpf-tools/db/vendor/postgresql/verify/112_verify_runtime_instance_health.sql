SELECT COUNT(*) AS cpf_runtime_instance_health_table_count
  FROM information_schema.tables
 WHERE table_schema = current_schema()
   AND table_name = 'cpf_runtime_instance_health';
SELECT COUNT(*) AS cpf_runtime_instance_health_index_count
  FROM pg_indexes
 WHERE schemaname = current_schema()
   AND indexname IN ('cpf_runtime_instance_health_pkey','ix_cpf_rih_last_seen','ix_cpf_rih_readiness');
