SELECT COUNT(*) AS cpf_runtime_instance_health_table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE()
   AND table_name = 'CPF_RUNTIME_INSTANCE_HEALTH';
SELECT COUNT(*) AS cpf_runtime_instance_health_index_count
  FROM information_schema.statistics
 WHERE table_schema = DATABASE()
   AND table_name = 'CPF_RUNTIME_INSTANCE_HEALTH'
   AND index_name IN ('PRIMARY','IX_CPF_RIH_LAST_SEEN','IX_CPF_RIH_READINESS');
