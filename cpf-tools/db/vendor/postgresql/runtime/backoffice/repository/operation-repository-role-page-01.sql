SELECT role_id AS roleId,role_code AS roleCode,role_name AS roleName,write_allowed_yn AS writeAllowedYn,data_scope AS dataScope,
       use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt FROM MBW_ROLE ORDER BY role_code LIMIT :limit OFFSET :offset
