SELECT login_history_id AS historyId, login_domain AS loginDomain, admin_user_id AS adminUserId,
       admin_login_id AS adminLoginId, login_result AS loginResult, failure_reason AS failureReason,
       client_ip AS clientIp, user_agent AS userAgent, transaction_id AS transactionId,
       system_code AS moduleId, application_name AS wasId, instance_id AS instanceId,
       created_at AS occurredAt
FROM mbw_login_history
ORDER BY login_history_id DESC
LIMIT :limit
