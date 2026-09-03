SELECT transfer_status AS transferStatus,
       endpoint_code AS endpointCode,
       local_path AS localPath,
       remote_path AS remotePath,
       checksum,
       file_size AS fileSize,
       completed_at AS completedAt,
       result_detail AS resultDetail
FROM CPF_FILE_TRANSFER_HISTORY
WHERE (? IS NULL OR endpoint_code = ?)
  AND (? IS NULL OR created_at >= ?)
  AND (? IS NULL OR created_at <= ?)
ORDER BY history_id DESC
LIMIT ?
