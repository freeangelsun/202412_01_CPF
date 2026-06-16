-- CPF framework initial code, response-code, message, and config data.
-- Target DB: cmnDB

USE cmnDB;

INSERT INTO code_table (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    (NULL, 'CODE_GROUP', 'MODULE', 'Service module code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', 'Request type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', 'Channel code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', 'Response result type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', 'Message format type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', 'Runtime log level code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', 'CMN cache name code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'WORKFLOW_STATUS', 'Workflow status code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'WORKFLOW_FAILURE_POLICY', 'Workflow failure policy code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'FILE_PROTOCOL', 'File exchange protocol code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', 'Yes or no code group', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO code_table (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'PFW', 'Framework common library', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', 'Common development library', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ACC', 'Account sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBR', 'Member sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'XYZ', 'Education sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', 'Admin service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', 'Normal request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', 'Compensation request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', 'Retry request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', 'Web channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', 'Mobile channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', 'Batch channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', 'Admin channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', 'Success response', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', 'Error response', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', 'Fixed message without parameters', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', 'Indexed parameter message using {0}, {1}', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'Trace logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'Debug logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'Info logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'Warning logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'Error logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', 'Common code cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', 'Common message cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', 'Common response code cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', 'Common config cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', 'All common caches', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'STARTED', 'Workflow started', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'SUCCESS', 'Workflow succeeded', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'FAILED', 'Workflow failed', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'ROLLBACK', 'Rollback on failure', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'VERIFY', 'Manual or automatic verification required', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'MANUAL', 'Manual follow-up required', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'IGNORE', 'Ignore failure', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'LOCAL', 'Local file protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'FTP', 'FTP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SFTP', 'SFTP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SCP', 'SCP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SSH', 'SSH command protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', 'Yes', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', 'No', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO message_table (
    message_code, locale, message_format_type, external_message, internal_message,
    parameter_count, parameter_sample, description, created_by, updated_by
) VALUES
    ('MPFW900001', 'ko', 'INDEXED', '?꾩닔 嫄곕옒 ?ㅻ뜑媛 ?꾨씫?섏뿀?듬땲??', 'PFW 嫄곕옒 ?ㅻ뜑 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW header validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900001', 'en', 'INDEXED', 'Required transaction header is missing.', 'PFW transaction header validation failed. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW header validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900002', 'ko', 'INDEXED', '嫄곕옒 硫뷀??곗씠???ㅼ젙???щ컮瑜댁? ?딆뒿?덈떎.', 'PFW @FpsTransaction 硫뷀??곗씠??寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. transactionId={0}', 1, '["MBR01BSE0001"]', 'PFW metadata validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900003', 'ko', 'INDEXED', '?쒕퉬???묒냽 ?뺣낫媛 ?놁뒿?덈떎.', 'PFW ?쒕퉬???붾뱶?ъ씤???ㅼ젙??李얠쓣 ???놁뒿?덈떎. serviceId={0}', 1, '["mbr"]', 'PFW endpoint message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900004', 'ko', 'INDEXED', '?숈쟻 濡쒓렇?덈꺼 ?ㅼ젙 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎.', 'PFW ?숈쟻 濡쒓렇?덈꺼 洹쒖튃 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'PFW dynamic log message', 'SYSTEM', 'SYSTEM'),
    ('MPFW990000', 'ko', 'INDEXED', '泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.', 'PFW ?대? ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. error={0}', 1, '["Exception"]', 'PFW internal error message', 'SYSTEM', 'SYSTEM'),
    ('MPFW990001', 'ko', 'INDEXED', '泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.', '?곗씠?곕쿋?댁뒪 泥섎━ ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. sqlState={0}', 1, '["HY000"]', 'PFW database message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010001', 'ko', 'INDEXED', '?붿껌 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.', '?붿껌 ?뚮씪誘명꽣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}, value={1}', 2, '["memberId","abc"]', 'PFW invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010002', 'ko', 'INDEXED', '?붿껌???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.', '議고쉶 ????곗씠?곌? 議댁옱?섏? ?딆뒿?덈떎. target={0}', 1, '["member"]', 'PFW not found message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010003', 'ko', 'INDEXED', '?대? ?깅줉???뺣낫?낅땲??', '以묐났 ?곗씠?곌? 媛먯??섏뿀?듬땲?? key={0}', 1, '["memberId"]', 'PFW duplicate message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010004', 'ko', 'INDEXED', '?낅젰媛믪쓣 ?뺤씤??二쇱꽭??', 'Bean Validation 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}', 1, '["name"]', 'PFW validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010005', 'ko', 'FIXED', '?몄쬆???꾩슂?⑸땲??', '?몄쬆?섏? ?딆? ?붿껌?낅땲??', 0, NULL, 'PFW unauthorized message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010006', 'ko', 'INDEXED', '泥섎━ 沅뚰븳???놁뒿?덈떎.', '?멸??섏? ?딆? ?붿껌?낅땲?? user={0}', 1, '["guest"]', 'PFW forbidden message', 'SYSTEM', 'SYSTEM'),
    ('MPFW020001', 'ko', 'INDEXED', '?붿껌??泥섎━?????놁뒿?덈떎.', '?낅Т 洹쒖튃 ?꾨컲??諛쒖깮?덉뒿?덈떎. rule={0}', 1, '["business-rule"]', 'PFW business rule message', 'SYSTEM', 'SYSTEM'),
    ('MPFW030001', 'ko', 'INDEXED', '?쇱떆?곸쑝濡?泥섎━?????놁뒿?덈떎.', '?몃? ?먮뒗 ? 二쇱젣?곸뿭 ?곌퀎 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. service={0}', 1, '["mbr"]', 'PFW external service message', 'SYSTEM', 'SYSTEM'),
    ('MPFW000000', 'ko', 'FIXED', '?뺤긽 泥섎━?섏뿀?듬땲??', 'PFW 怨듯넻 ?붿껌???뺤긽 泥섎━?섏뿀?듬땲??', 0, NULL, 'PFW common success message', 'SYSTEM', 'SYSTEM'),
    ('MACC000000', 'ko', 'FIXED', '?깃났', 'ACC ?붿껌???뺤긽 泥섎━?섏뿀?듬땲??', 0, NULL, 'ACC success message', 'SYSTEM', 'SYSTEM'),
    ('MACC010000', 'ko', 'FIXED', '?깃났', 'ACC ?낅Т ?붿껌???뺤긽 泥섎━?섏뿀?듬땲??', 0, NULL, 'ACC business success message', 'SYSTEM', 'SYSTEM'),
    ('MACC010001', 'ko', 'INDEXED', '?붿껌 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.', 'ACC ?뚮씪誘명꽣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}', 1, '["accountId"]', 'ACC invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MACC010002', 'ko', 'INDEXED', '?붿껌???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.', 'ACC 議고쉶 ??곸씠 ?놁뒿?덈떎. target={0}', 1, '["account"]', 'ACC not found message', 'SYSTEM', 'SYSTEM'),
    ('MMBR000000', 'ko', 'FIXED', '?깃났', 'MBR ?붿껌???뺤긽 泥섎━?섏뿀?듬땲??', 0, NULL, 'MBR success message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010001', 'ko', 'FIXED', '?앹꽦 ?깃났', 'MBR ?곗씠?곌? ?앹꽦?섏뿀?듬땲??', 0, NULL, 'MBR created message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010002', 'ko', 'FIXED', '?섏젙 ?깃났', 'MBR ?곗씠?곌? ?섏젙?섏뿀?듬땲??', 0, NULL, 'MBR updated message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010003', 'ko', 'FIXED', '??젣 ?깃났', 'MBR ?곗씠?곌? ??젣?섏뿀?듬땲??', 0, NULL, 'MBR deleted message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010101', 'ko', 'FIXED', '?섎せ???붿껌?낅땲??', 'MBR ?붿껌 ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.', 0, NULL, 'MBR bad request message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010102', 'ko', 'INDEXED', '?좏슚?섏? ?딆? ?뚮씪誘명꽣?낅땲??', 'MBR ?뚮씪誘명꽣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}', 1, '["memberId"]', 'MBR invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010103', 'ko', 'INDEXED', '?붿껌???먯썝??李얠쓣 ???놁뒿?덈떎.', 'MBR 議고쉶 ??곸씠 ?놁뒿?덈떎. target={0}', 1, '["member"]', 'MBR not found message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010104', 'ko', 'INDEXED', '以묐났???곗씠?곌? ?덉뒿?덈떎.', 'MBR 以묐났 ?곗씠?곌? 媛먯??섏뿀?듬땲?? key={0}', 1, '["memberId"]', 'MBR duplicate message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010105', 'ko', 'INDEXED', '?낅젰媛?寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎.', 'MBR ?낅젰媛?寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}', 1, '["name"]', 'MBR validation message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010106', 'ko', 'FIXED', '?몄쬆???꾩슂?⑸땲??', 'MBR ?몄쬆?섏? ?딆? ?붿껌?낅땲??', 0, NULL, 'MBR unauthorized message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010107', 'ko', 'FIXED', '?묎렐 沅뚰븳???놁뒿?덈떎.', 'MBR ?묎렐 沅뚰븳???놁뒿?덈떎.', 0, NULL, 'MBR forbidden message', 'SYSTEM', 'SYSTEM'),
    ('MMBR030001', 'ko', 'INDEXED', '?몃? ?쒕퉬???ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.', 'MBR ?몃? ?쒕퉬???ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. service={0}', 1, '["external"]', 'MBR external service message', 'SYSTEM', 'SYSTEM'),
    ('MMBR990000', 'ko', 'INDEXED', '?대? ?쒕쾭 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.', 'MBR ?대? ?쒕쾭 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. error={0}', 1, '["Exception"]', 'MBR internal server message', 'SYSTEM', 'SYSTEM'),
    ('MMBR990001', 'ko', 'INDEXED', '?곗씠?곕쿋?댁뒪 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.', 'MBR ?곗씠?곕쿋?댁뒪 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. sqlState={0}', 1, '["HY000"]', 'MBR database message', 'SYSTEM', 'SYSTEM'),
    ('MXYZ090001', 'ko', 'INDEXED', '?대? ?깅줉??{0}?낅땲??', '{0}={1} 媛믪씠 ?대? 議댁옱?⑸땲?? duplicateCheck=XYZ_EDU_SAMPLE', 2, '["?뚯썝踰덊샇","M0001"]', 'XYZ dynamic duplicate education message', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF sample system welcome.', 'CMN welcome sample message.', 0, NULL, 'Sample welcome message', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF sample system.', 'CMN welcome sample message.', 0, NULL, 'Sample welcome message', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_format_type = VALUES(message_format_type),
    external_message = VALUES(external_message),
    internal_message = VALUES(internal_message),
    parameter_count = VALUES(parameter_count),
    parameter_sample = VALUES(parameter_sample),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO response_code_table (
    response_code, message_code, result_type, module_id, response_group, sequence_no,
    http_status, description, created_by, updated_by
) VALUES
    ('SPFW000000', 'MPFW000000', 'S', 'PFW', '00', '0000', 200, 'PFW common success', 'SYSTEM', 'SYSTEM'),
    ('EPFW010001', 'MPFW010001', 'E', 'PFW', '01', '0001', 400, 'Invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EPFW010002', 'MPFW010002', 'E', 'PFW', '01', '0002', 404, 'Not found', 'SYSTEM', 'SYSTEM'),
    ('EPFW010003', 'MPFW010003', 'E', 'PFW', '01', '0003', 409, 'Duplicate', 'SYSTEM', 'SYSTEM'),
    ('EPFW010004', 'MPFW010004', 'E', 'PFW', '01', '0004', 400, 'Validation failed', 'SYSTEM', 'SYSTEM'),
    ('EPFW010005', 'MPFW010005', 'E', 'PFW', '01', '0005', 401, 'Unauthorized', 'SYSTEM', 'SYSTEM'),
    ('EPFW010006', 'MPFW010006', 'E', 'PFW', '01', '0006', 403, 'Forbidden', 'SYSTEM', 'SYSTEM'),
    ('EPFW020001', 'MPFW020001', 'E', 'PFW', '02', '0001', 400, 'Business rule violation', 'SYSTEM', 'SYSTEM'),
    ('EPFW030001', 'MPFW030001', 'E', 'PFW', '03', '0001', 502, 'External service error', 'SYSTEM', 'SYSTEM'),
    ('EPFW900001', 'MPFW900001', 'E', 'PFW', '90', '0001', 400, 'Missing transaction header', 'SYSTEM', 'SYSTEM'),
    ('EPFW900002', 'MPFW900002', 'E', 'PFW', '90', '0002', 500, 'Invalid transaction metadata', 'SYSTEM', 'SYSTEM'),
    ('EPFW900003', 'MPFW900003', 'E', 'PFW', '90', '0003', 500, 'Service endpoint not found', 'SYSTEM', 'SYSTEM'),
    ('EPFW900004', 'MPFW900004', 'E', 'PFW', '90', '0004', 400, 'Dynamic log rule invalid', 'SYSTEM', 'SYSTEM'),
    ('EPFW990000', 'MPFW990000', 'E', 'PFW', '99', '0000', 500, 'Internal server error', 'SYSTEM', 'SYSTEM'),
    ('EPFW990001', 'MPFW990001', 'E', 'PFW', '99', '0001', 500, 'Database error', 'SYSTEM', 'SYSTEM'),
    ('SACC000000', 'MACC000000', 'S', 'ACC', '00', '0000', 200, 'ACC common success', 'SYSTEM', 'SYSTEM'),
    ('SACC010000', 'MACC010000', 'S', 'ACC', '01', '0000', 200, 'ACC business success', 'SYSTEM', 'SYSTEM'),
    ('EACC010001', 'MACC010001', 'E', 'ACC', '01', '0001', 400, 'ACC invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EACC010002', 'MACC010002', 'E', 'ACC', '01', '0002', 404, 'ACC not found', 'SYSTEM', 'SYSTEM'),
    ('SMBR000000', 'MMBR000000', 'S', 'MBR', '00', '0000', 200, 'MBR common success', 'SYSTEM', 'SYSTEM'),
    ('SMBR010001', 'MMBR010001', 'S', 'MBR', '01', '0001', 200, 'MBR created', 'SYSTEM', 'SYSTEM'),
    ('SMBR010002', 'MMBR010002', 'S', 'MBR', '01', '0002', 200, 'MBR updated', 'SYSTEM', 'SYSTEM'),
    ('SMBR010003', 'MMBR010003', 'S', 'MBR', '01', '0003', 200, 'MBR deleted', 'SYSTEM', 'SYSTEM'),
    ('EMBR010001', 'MMBR010101', 'E', 'MBR', '01', '0001', 400, 'MBR bad request', 'SYSTEM', 'SYSTEM'),
    ('EMBR010002', 'MMBR010102', 'E', 'MBR', '01', '0002', 400, 'MBR invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EMBR010003', 'MMBR010103', 'E', 'MBR', '01', '0003', 404, 'MBR not found', 'SYSTEM', 'SYSTEM'),
    ('EMBR010004', 'MMBR010104', 'E', 'MBR', '01', '0004', 409, 'MBR duplicate', 'SYSTEM', 'SYSTEM'),
    ('EMBR010005', 'MMBR010105', 'E', 'MBR', '01', '0005', 400, 'MBR validation failed', 'SYSTEM', 'SYSTEM'),
    ('EMBR010006', 'MMBR010106', 'E', 'MBR', '01', '0006', 401, 'MBR unauthorized', 'SYSTEM', 'SYSTEM'),
    ('EMBR010007', 'MMBR010107', 'E', 'MBR', '01', '0007', 403, 'MBR forbidden', 'SYSTEM', 'SYSTEM'),
    ('EMBR030001', 'MMBR030001', 'E', 'MBR', '03', '0001', 502, 'MBR external service error', 'SYSTEM', 'SYSTEM'),
    ('EMBR990000', 'MMBR990000', 'E', 'MBR', '99', '0000', 500, 'MBR internal server error', 'SYSTEM', 'SYSTEM'),
    ('EMBR990001', 'MMBR990001', 'E', 'MBR', '99', '0001', 500, 'MBR database error', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_code = VALUES(message_code),
    result_type = VALUES(result_type),
    module_id = VALUES(module_id),
    response_group = VALUES(response_group),
    sequence_no = VALUES(sequence_no),
    http_status = VALUES(http_status),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO config_table (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES
    ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'Preload CMN cache at startup', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', 'Fail startup when CMN cache preload fails', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', 'Cache refresh event polling interval in milliseconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.PERIODIC_REFRESH_MILLIS', '1800000', 'NUMBER', 'Periodic cache refresh interval in milliseconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', 'Default CMN message broker type', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.DEFAULT_DESTINATION', 'cpf.default.event', 'STRING', 'Default messaging destination', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.SSH_ENABLED', 'N', 'BOOLEAN', 'Allow SSH/SCP/SFTP execution', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.DRY_RUN', 'Y', 'BOOLEAN', 'Plan remote file operations without execution', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.TIMEOUT_SECONDS', '15', 'NUMBER', 'File exchange timeout seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.ISSUER', 'CPF', 'STRING', 'Sample JWT issuer', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.AUDIENCE', 'CPF-API', 'STRING', 'Sample JWT audience', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.TTL_SECONDS', '300', 'NUMBER', 'Sample JWT TTL seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'PFW HTTP client connect timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'PFW HTTP client read timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.SESSION_TTL_SECONDS', '3600', 'NUMBER', 'ADM session TTL seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'ADM password expiration days', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MIN_LENGTH', '10', 'NUMBER', 'ADM password minimum length', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MAX_FAIL_COUNT', '5', 'NUMBER', 'ADM login failure lock threshold', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.FEATURE.SAMPLE_ENABLED', 'Y', 'BOOLEAN', 'Enable sample APIs and education flows', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    config_type = VALUES(config_type),
    description = VALUES(description),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO security_jwt_key (
    KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, CREATED_BY, UPDATED_BY
) VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    ISSUER = VALUES(ISSUER),
    ALGORITHM = VALUES(ALGORITHM),
    SECRET_REF = VALUES(SECRET_REF),
    ACTIVE_YN = VALUES(ACTIVE_YN),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO cache_refresh_event (
    cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by
)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);
