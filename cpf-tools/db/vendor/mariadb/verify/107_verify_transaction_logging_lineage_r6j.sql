SELECT COUNT(*) AS lineage_table_count FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN ('cpf_transaction_lineage','cpf_transaction_lineage_archive');
SELECT index_name FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name IN ('cpf_transaction_lineage','cpf_transaction_lineage_archive') GROUP BY index_name ORDER BY index_name;
SELECT partition_method FROM information_schema.partitions WHERE table_schema=DATABASE() AND table_name='cpf_transaction_lineage' AND partition_name IS NOT NULL LIMIT 1;
