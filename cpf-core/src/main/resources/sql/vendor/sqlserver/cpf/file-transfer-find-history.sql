SELECT transfer_status AS transferStatus,
       endpoint_code AS endpointCode,
       local_path AS localPath,
       remote_path AS remotePath,
       checksum,
       file_size AS fileSize,
       completed_at AS completedAt,
       result_detail AS resultDetail
FROM cpf_file_transfer_history
WHERE (? IS NULL OR endpoint_code = ?)
  AND (? IS NULL OR created_at >= ?)
  AND (? IS NULL OR created_at <= ?)
ORDER BY history_id DESC
OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
