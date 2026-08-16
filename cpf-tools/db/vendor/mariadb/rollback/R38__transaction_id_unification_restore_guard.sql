-- CPF R38 rollback guard
-- V38은 parent/child/root 거래 식별자를 단일 transactionId + segment hierarchy로 수렴시키는 Contract 변경을 포함한다.
-- 이 정보 모델 변경을 자동 역변환하면 원래 parent/child 의미를 재구성할 수 없어 잘못된 감사/복구 데이터를 만들 수 있다.
-- 따라서 자동 destructive rollback을 제공하지 않는다.
-- Rollback 필요 시: (1) 적용 직전 DB backup restore 또는 (2) 승인된 forward-recovery migration을 사용한다.
SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'CPF R38 automatic rollback is intentionally blocked; restore pre-V38 backup or use approved forward recovery.';
