DELETE FROM mbw_menu
 WHERE menu_code = :menuCode
   AND version_no = :expectedVersion
