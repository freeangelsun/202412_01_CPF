DELETE FROM MBW_MENU
 WHERE menu_code = :menuCode
   AND version_no = :expectedVersion
