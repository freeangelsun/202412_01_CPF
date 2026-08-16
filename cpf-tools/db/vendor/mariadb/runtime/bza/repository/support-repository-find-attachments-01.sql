SELECT attachment_id AS attachmentId, attachment_group_id AS attachmentGroupId,
       original_file_name AS originalFileName, content_type AS contentType,
       file_size AS fileSize, checksum_sha256 AS checksumSha256,
       scan_status AS scanStatus, data_classification AS dataClassification, retention_until AS retentionUntil, quarantine_yn AS quarantineYn, created_by AS createdBy, created_at AS createdAt
  FROM bza_attachment
 WHERE attachment_group_id = :groupId AND use_yn = 'Y'
 ORDER BY attachment_id
