SELECT (SELECT COUNT(*) FROM MBW_ADMIN_USER WHERE use_yn = 'Y') AS activeUserCount,
       (SELECT COUNT(*) FROM MBW_EMPLOYEE WHERE use_yn = 'Y' AND employment_status IN ('EMPLOYED','SECONDMENT','DISPATCHED')) AS activeEmployeeCount,
       (SELECT COUNT(*) FROM MBW_APPROVAL_DOCUMENT WHERE approval_status = 'IN_REVIEW') AS pendingApprovalCount,
       (SELECT COUNT(*) FROM MBW_NOTIFICATION
         WHERE recipient_login_id = :loginId AND read_yn = 'N' AND use_yn = 'Y') AS unreadNotificationCount,
       (SELECT COUNT(*) FROM MBW_BUSINESS_AUDIT
         WHERE created_at >= CURRENT_DATE) AS todayAuditCount
