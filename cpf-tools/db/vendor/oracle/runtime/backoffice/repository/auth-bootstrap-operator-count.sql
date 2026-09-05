-- 최초 설치 전인지 판정한다.
-- 인증에 쓸 수 있는 자격이 없는 행(password_hash IS NULL)은 로그인할 수 없으므로 설치 완료로
-- 보지 않는다. 제품 seed 의 자격 없는 샘플 운영자가 최초 운영자 생성을 영구히 막던 결함을 막는다.
SELECT COUNT(*)
FROM MBW_ADMIN_USER
WHERE password_hash IS NOT NULL
