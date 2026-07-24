SELECT audit_id AS auditId, transaction_global_id AS transactionGlobalId,
       actor_id AS actorId, action_type AS actionType, target_type AS targetType,
       target_id AS targetId, reason, before_data AS beforeData,
       after_data AS afterData, created_at AS createdAt
FROM bza_business_audit
ORDER BY audit_id DESC
LIMIT :limit
