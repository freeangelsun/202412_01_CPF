MERGE INTO bza_menu target
USING (
    SELECT :menuCode menu_code, :menuName menu_name, :parentMenuCode parent_menu_code,
           :moduleCode module_code, :routePath route_path, :iconCode icon_code,
           :environmentCode environment_code, :apiPath api_path, :sortOrder sort_order,
           :useYn use_yn, :requestUser request_user
    FROM dual
) source
ON (target.menu_code = source.menu_code)
WHEN MATCHED THEN UPDATE SET
    target.menu_name = source.menu_name, target.parent_menu_code = source.parent_menu_code,
    target.module_code = source.module_code, target.route_path = source.route_path,
    target.icon_code = source.icon_code, target.environment_code = source.environment_code,
    target.api_path = source.api_path, target.sort_order = source.sort_order,
    target.use_yn = source.use_yn, target.updated_by = source.request_user,
    target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    source.menu_code, source.menu_name, source.parent_menu_code, source.module_code,
    source.route_path, source.icon_code, source.environment_code, source.api_path,
    source.sort_order, source.use_yn, source.request_user, source.request_user
)
