SELECT table_name FROM information_schema.tables
 WHERE table_schema = DATABASE()
   AND table_name IN ('CPF_CACHE_INVALIDATION_EVENT','CPF_CACHE_INVALIDATION_CHECKPOINT')
 ORDER BY table_name;
