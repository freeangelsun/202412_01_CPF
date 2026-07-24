-- MBR 미등록 loginId 실패도 로그인 이력에 남길 수 있도록 회원 순번을 nullable로 보강합니다.
USE mbrDB;

ALTER TABLE mbr_member_login_history
    MODIFY member_id BIGINT NULL COMMENT '회원 순번';
