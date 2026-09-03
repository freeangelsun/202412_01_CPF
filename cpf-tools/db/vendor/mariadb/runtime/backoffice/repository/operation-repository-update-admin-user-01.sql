UPDATE MBW_ADMIN_USER
   SET admin_name=:adminName,
       password_hash=COALESCE(:passwordHash,password_hash),
       account_status=:accountStatus,
       use_yn=:useYn,
       lock_yn=:lockYn,
       password_change_required_yn=:passwordChangeRequiredYn,
       version_no=version_no+1,
       updated_by=:requestUser,
       updated_at=CURRENT_TIMESTAMP
 WHERE admin_login_id=:loginId AND version_no=:expectedVersion
