-- 최초 운영자는 설치자가 직접 정한 bootstrap secret 으로 만들어진다. 그 secret 은 어디에도
-- 저장되지 않으며 설치자만 알고 있으므로 즉시 사용 가능해야 한다. 강제 변경을 켜 두면 첫
-- 로그인이 항상 거절되어 공개 Consumer 가 업무 화면에 들어갈 수 없다(ADM 최초 운영자와 같은 계약).
INSERT INTO MBW_ADMIN_USER (
    admin_login_id, admin_name, password_hash, role_code,
    account_status, version_no, create_operation_id, use_yn, lock_yn,
    login_fail_count, password_change_required_yn, password_expire_at, created_by, updated_by
) VALUES (
    :loginId, :operatorName, :passwordHash, :roleCode,
    'ACTIVE', 0, :operationId, 'Y', 'N',
    0, 'N', :passwordExpireAt, 'BOOTSTRAP', 'BOOTSTRAP'
)
