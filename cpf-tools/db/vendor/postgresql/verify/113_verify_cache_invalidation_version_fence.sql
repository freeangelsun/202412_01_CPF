SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'cpf_cache_invalidation_version';
