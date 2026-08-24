SELECT center_cut_item_id AS targetId,
       center_cut_execution_id AS executionId,
       center_cut_job_id AS centerCutJobId,
       business_key AS businessKey,
       business_date AS businessDate,
       item_status AS statusCode,
       retry_count AS retryCount,
       transaction_id AS transactionId,
       parent_segment_id AS parentSegmentId,
       transaction_segment_id AS transactionSegmentId,
       started_at AS startedAt,
       completed_at AS completedAt,
       last_error_message AS lastErrorMessage,
       CHAR_LENGTH(item_payload) AS targetPayloadLength,
       created_at AS createdAt,
       updated_at AS updatedAt
FROM BAT_CENTER_CUT_ITEM
WHERE center_cut_job_id = ?
  AND (? IS NULL OR item_status = ?)
ORDER BY center_cut_item_id
LIMIT ?
