INSERT INTO bza_menu (
    menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    :menuCode, :menuName, :parentMenuCode, :moduleCode, :routePath, :iconCode,
    :environmentCode, :apiPath, :sortOrder, :useYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name), parent_menu_code = VALUES(parent_menu_code),
    module_code = VALUES(module_code), route_path = VALUES(route_path), icon_code = VALUES(icon_code),
    environment_code = VALUES(environment_code), api_path = VALUES(api_path),
    sort_order = VALUES(sort_order), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
