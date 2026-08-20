UPDATE mbw_notification
   SET read_yn = 'Y', read_at = CURRENT_TIMESTAMP,
       updated_by = :requestUser, updated_at = CURRENT_TIMESTAMP
 WHERE notification_id = :notificationId
   AND recipient_login_id = :loginId
   AND use_yn = 'Y'
