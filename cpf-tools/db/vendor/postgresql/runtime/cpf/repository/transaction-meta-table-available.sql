SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = CURRENT_SCHEMA()
  AND table_name = 'cpf_transaction_meta'
