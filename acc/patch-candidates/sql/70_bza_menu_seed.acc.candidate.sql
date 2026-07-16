-- Account BZA 업무 메뉴 seed 후보입니다.
INSERT INTO bza_menu (
    menu_code, menu_name, parent_menu_code, module_code, route_path,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    'ACC_ROOT', 'Account', NULL, 'ACC', '/bza/domain/acc',
    'ALL', '/api/v1/acc', 900, 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    module_code = VALUES(module_code),
    route_path = VALUES(route_path),
    api_path = VALUES(api_path),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;