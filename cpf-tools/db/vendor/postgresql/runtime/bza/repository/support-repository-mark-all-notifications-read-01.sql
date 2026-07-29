UPDATE bza_notification SET read_yn='Y',read_at=CURRENT_TIMESTAMP,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP WHERE recipient_login_id=:loginId AND read_yn='N' AND use_yn='Y'
