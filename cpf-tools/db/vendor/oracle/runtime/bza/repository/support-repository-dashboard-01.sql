SELECT (SELECT COUNT(*) FROM bza_admin_user WHERE use_yn = 'Y') AS activeUserCount,
       (SELECT COUNT(*) FROM bza_employee WHERE use_yn = 'Y' AND employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')) AS activeEmployeeCount,
       (SELECT COUNT(*) FROM bza_approval_document WHERE approval_status = 'IN_REVIEW') AS pendingApprovalCount,
       (SELECT COUNT(*) FROM bza_notification
         WHERE recipient_login_id = :loginId AND read_yn = 'N' AND use_yn = 'Y') AS unreadNotificationCount,
       (SELECT COUNT(*) FROM bza_business_audit
         WHERE created_at >= CURRENT_DATE) AS todayAuditCount
