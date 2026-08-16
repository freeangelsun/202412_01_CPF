SELECT COUNT(*) AS lineage_table_count FROM information_schema.tables WHERE table_name IN ('cpf_transaction_lineage','cpf_transaction_lineage_archive');
SELECT indexname FROM pg_indexes WHERE tablename IN ('cpf_transaction_lineage','cpf_transaction_lineage_archive') ORDER BY indexname;
SELECT partstrat FROM pg_partitioned_table p JOIN pg_class c ON c.oid=p.partrelid WHERE c.relname='cpf_transaction_lineage';
