-- Account 표준 실행 카탈로그 바로가기 메뉴 seed 후보입니다.
INSERT INTO adm_menu (menu_id, parent_menu_id, menu_name, menu_path, sort_order, use_yn, created_by, updated_by)
VALUES ('ACC_STANDARD_EXECUTION', 'STANDARD_EXECUTION', 'Account 실행 정의', '/adm#standard-executions', 900, 'Y', 'create-domain', 'create-domain')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_path = VALUES(menu_path), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;