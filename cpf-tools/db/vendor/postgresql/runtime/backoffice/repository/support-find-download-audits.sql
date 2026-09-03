SELECT download_audit_id AS downloadAuditId, actor_id AS actorId,
       download_code AS downloadCode, reason, filter_json AS filterJson,
       row_count AS rowCount, result_status AS resultStatus, file_name AS fileName,
       masking_applied_yn AS maskingAppliedYn,
       transaction_id AS transactionId, created_at AS createdAt
FROM MBW_DOWNLOAD_AUDIT
ORDER BY download_audit_id DESC
LIMIT :limit
