-- Runtime Operation Catalog is bootstrapped automatically; the legacy manual scan endpoint no longer exists.
DELETE FROM ADM_BUTTON
WHERE BUTTON_ID = 'TRANSACTION_META_SCAN'
  AND MENU_ID = 'TRANSACTION_META';
