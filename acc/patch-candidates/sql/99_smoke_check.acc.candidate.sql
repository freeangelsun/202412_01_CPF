-- Account smoke check 후보입니다.
SELECT 'ACC_SAMPLE_TABLE' AS check_id, COUNT(*) AS row_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'acc_sample';