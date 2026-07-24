INSERT INTO bza_menu (
    menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    :menuCode, :menuName, :parentMenuCode, :moduleCode, :routePath, :iconCode,
    :environmentCode, :apiPath, :sortOrder, :useYn, :requestUser, :requestUser
)
ON CONFLICT (menu_code) DO UPDATE SET
    menu_name = EXCLUDED.menu_name, parent_menu_code = EXCLUDED.parent_menu_code,
    module_code = EXCLUDED.module_code, route_path = EXCLUDED.route_path,
    icon_code = EXCLUDED.icon_code, environment_code = EXCLUDED.environment_code,
    api_path = EXCLUDED.api_path, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
