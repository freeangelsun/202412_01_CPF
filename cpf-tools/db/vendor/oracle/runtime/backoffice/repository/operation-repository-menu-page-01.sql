SELECT menu_id AS menuId,menu_code AS menuCode,menu_name AS menuName,parent_menu_code AS parentMenuCode,module_code AS moduleCode,
       route_path AS routePath,icon_code AS iconCode,environment_code AS environmentCode,api_path AS apiPath,sort_order AS sortOrder,
       use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
  FROM mbw_menu ORDER BY module_code,sort_order,menu_code OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
