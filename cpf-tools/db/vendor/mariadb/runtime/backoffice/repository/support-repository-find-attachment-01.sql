SELECT attachment_id AS attachmentId, attachment_group_id AS attachmentGroupId,
       original_file_name AS originalFileName, stored_file_name AS storedFileName,
       storage_key AS storageKey, content_type AS contentType, file_size AS fileSize,
       checksum_sha256 AS checksumSha256, scan_status AS scanStatus, data_classification AS dataClassification, retention_until AS retentionUntil, quarantine_yn AS quarantineYn, use_yn AS useYn
  FROM MBW_ATTACHMENT
 WHERE attachment_id = :attachmentId
