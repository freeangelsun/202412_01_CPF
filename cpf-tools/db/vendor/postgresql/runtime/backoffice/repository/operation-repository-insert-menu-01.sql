INSERT INTO mbw_menu(menu_code,menu_name,parent_menu_code,module_code,route_path,icon_code,environment_code,api_path,sort_order,use_yn,version_no,created_by,updated_by)
VALUES(:menuCode,:menuName,:parentMenuCode,:moduleCode,:routePath,:iconCode,:environmentCode,:apiPath,:sortOrder,:useYn,0,:requestUser,:requestUser)
