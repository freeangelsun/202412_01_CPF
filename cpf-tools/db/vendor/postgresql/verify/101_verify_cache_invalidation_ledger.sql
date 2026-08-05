SELECT table_name FROM information_schema.tables
 WHERE table_schema = current_schema()
   AND table_name IN ('cpf_cache_invalidation_event','cpf_cache_invalidation_checkpoint')
 ORDER BY table_name;
