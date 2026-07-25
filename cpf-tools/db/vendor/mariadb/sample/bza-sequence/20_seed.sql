-- Optional BZA sequence customization sample seed. Run only after 10_install.sql and only when the sample is enabled.
INSERT INTO bza_sequence_sample_rule(
    rule_code, rule_name, prefix, current_value, padding_length, use_yn, created_by, updated_by
) VALUES(
    'ORDER', '주문번호 Sample', 'ORD-', 0, 8, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    rule_name=VALUES(rule_name), prefix=VALUES(prefix), padding_length=VALUES(padding_length),
    use_yn=VALUES(use_yn), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO bza_menu(
    menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code,
    environment_code, api_path, sort_order, use_yn, created_by, updated_by
) VALUES(
    'BZA_SEQUENCE_SAMPLE', '업무 채번 Sample', NULL, 'BZA', '/bza/sequence-sample', 'sequence',
    'ALL', '/api/bza/sample/sequence', 900, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    menu_name=VALUES(menu_name), route_path=VALUES(route_path), api_path=VALUES(api_path),
    sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO bza_permission(
    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
    domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by
) VALUES
    ('BZA_ADMIN','BZA_SEQUENCE_SAMPLE','READ','API','GET','/api/bza/sample/sequence/**',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM'),
    ('BZA_ADMIN','BZA_SEQUENCE_SAMPLE','WRITE','API','POST','/api/bza/sample/sequence/**',NULL,'ALL','ALL','Y','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE
    permission_type=VALUES(permission_type), http_method=VALUES(http_method), api_pattern=VALUES(api_pattern),
    allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP(3);
