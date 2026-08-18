-- Roll back V126 when an older application version still exposes the legacy scan endpoint.
INSERT IGNORE INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, created_at, updated_by, updated_at)
VALUES ('TRANSACTION_META_SCAN', 'TRANSACTION_META', 'SCAN', '거래 메타 스캔', 'POST', '/adm/api/transactions/scan', 20, 'Y', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP);
