SELECT COUNT(*)
FROM CPF_FILE_TRANSFER_HISTORY
WHERE endpoint_code = ?
  AND duplicate_key = ?
  AND (? IS NULL OR checksum = ?)
  AND transfer_status = 'SUCCESS'
