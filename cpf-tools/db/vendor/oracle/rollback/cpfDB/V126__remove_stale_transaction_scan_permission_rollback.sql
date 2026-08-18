-- Roll back V126 when an older application version still exposes the legacy scan endpoint.
MERGE INTO ADM_BUTTON t
USING (SELECT 'TRANSACTION_META_SCAN' BUTTON_ID, 'TRANSACTION_META' MENU_ID, 'SCAN' ACTION_CODE, '거래 메타 스캔' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/transactions/scan' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual) s
ON (t.BUTTON_ID = s.BUTTON_ID)
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, created_at, updated_by, updated_at)
VALUES (s.BUTTON_ID, s.MENU_ID, s.ACTION_CODE, s.BUTTON_NAME, s.HTTP_METHOD, s.API_PATTERN, s.SORT_ORDER, s.USE_YN, s.created_by, CURRENT_TIMESTAMP, s.updated_by, CURRENT_TIMESTAMP);
