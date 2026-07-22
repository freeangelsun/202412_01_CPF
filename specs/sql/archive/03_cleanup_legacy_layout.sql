-- CPF 로컬/테스트 레거시 레이아웃 정리 스크립트입니다.
-- 표준 테이블명으로 재생성하기 전에 예전 이름의 샘플 테이블을 제거합니다.

-- 과거 실험/초기 개발 DB 정리용 archive SQL입니다.
-- 공식 CPF 신규 설치 흐름에서는 사용하지 않습니다.
-- 신규 기준본은 `cpf_*`, `cmn_*`, `adm_*`, `acc_*`, `mbr_*` 현행 테이블명만 사용합니다.

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS cpfDB.cpf_transaction_log_detail;
DROP TABLE IF EXISTS cpfDB.cpf_transaction_log;
DROP TABLE IF EXISTS cpfDB.TRAN_LOG_DTL;
DROP TABLE IF EXISTS cpfDB.TRAN_LOG;
DROP TABLE IF EXISTS cpfDB.code_table;
DROP TABLE IF EXISTS cpfDB.message_table;
DROP TABLE IF EXISTS cpfDB.response_code_table;
DROP TABLE IF EXISTS cpfDB.config_table;
DROP TABLE IF EXISTS cpfDB.cache_refresh_event;
DROP TABLE IF EXISTS cpfDB.file_exchange_log;
DROP TABLE IF EXISTS cpfDB.security_jwt_key;
DROP TABLE IF EXISTS cpfDB.security_token_audit_log;

DROP TABLE IF EXISTS admDB.operator_user_role;
DROP TABLE IF EXISTS admDB.operator_role_menu;
DROP TABLE IF EXISTS admDB.operator_audit_log;
DROP TABLE IF EXISTS admDB.operator_session;
DROP TABLE IF EXISTS admDB.operator_menu;
DROP TABLE IF EXISTS admDB.operator_role;
DROP TABLE IF EXISTS admDB.operator_user;
DROP TABLE IF EXISTS admDB.dynamic_log_level_rule;

DROP TABLE IF EXISTS admDB.member;
DROP TABLE IF EXISTS accDB.acc_member;
DROP TABLE IF EXISTS mbrDB.member;
DROP TABLE IF EXISTS cmnDB.cmn_member;
DROP TABLE IF EXISTS cmnDB.code_table;
DROP TABLE IF EXISTS cmnDB.message_table;
DROP TABLE IF EXISTS cmnDB.response_code_table;
DROP TABLE IF EXISTS cmnDB.config_table;
DROP TABLE IF EXISTS cmnDB.cache_refresh_event;

SET FOREIGN_KEY_CHECKS = 1;
