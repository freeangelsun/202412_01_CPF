SELECT notification_id AS notificationId, notification_type AS notificationType,
       title, message_body AS messageBody, reference_type AS referenceType,
       reference_id AS referenceId, read_yn AS readYn, read_at AS readAt, created_at AS createdAt
FROM bza_notification
WHERE recipient_login_id = :loginId AND use_yn = 'Y'
  AND (:unreadOnly = 'N' OR read_yn = 'N')
ORDER BY notification_id DESC
LIMIT :limit
