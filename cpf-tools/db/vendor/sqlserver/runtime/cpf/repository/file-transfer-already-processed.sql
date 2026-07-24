SELECT COUNT(*)
FROM cpf_file_transfer_history
WHERE endpoint_code = ?
  AND duplicate_key = ?
  AND (? IS NULL OR checksum = ?)
  AND transfer_status = 'SUCCESS'
