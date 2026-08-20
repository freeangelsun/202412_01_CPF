INSERT INTO mbw_user_role(admin_user_id,role_code,valid_from,valid_to,primary_yn,grant_reason,operation_id,version_no,created_by,updated_by)
VALUES(:adminUserId,:roleCode,:validFrom,:validTo,:primaryYn,:grantReason,:operationId,0,:operatorId,:operatorId)
