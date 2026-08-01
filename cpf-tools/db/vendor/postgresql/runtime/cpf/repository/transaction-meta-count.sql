SELECT COUNT(*)
FROM cpf_transaction_meta
WHERE (? IS NULL OR module_code = ?)
  AND (? IS NULL OR active_yn = ?)
  AND (? IS NULL OR transaction_id LIKE ?)
