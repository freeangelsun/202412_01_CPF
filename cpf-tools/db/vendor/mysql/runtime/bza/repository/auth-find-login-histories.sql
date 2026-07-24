SELECT login_history_id AS historyId, login_domain AS loginDomain, admin_user_id AS adminUserId,
       admin_login_id AS adminLoginId, login_result AS loginResult, failure_reason AS failureReason,
       client_ip AS clientIp, user_agent AS userAgent, transaction_global_id AS transactionGlobalId,
       module_id AS moduleId, was_id AS wasId, server_instance_id AS serverInstanceId,
       created_at AS occurredAt
FROM bza_login_history
ORDER BY login_history_id DESC
LIMIT :limit
