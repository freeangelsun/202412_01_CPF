SELECT COUNT(*) AS lineage_table_count FROM user_tables WHERE table_name IN ('CPF_TRANSACTION_LINEAGE','CPF_TRANSACTION_LINEAGE_ARCHIVE');
SELECT index_name, table_name FROM user_indexes WHERE table_name IN ('CPF_TRANSACTION_LINEAGE','CPF_TRANSACTION_LINEAGE_ARCHIVE') ORDER BY table_name,index_name;
SELECT table_name, partitioned FROM user_tables WHERE table_name='CPF_TRANSACTION_LINEAGE';
