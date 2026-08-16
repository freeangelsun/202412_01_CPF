SELECT setting_key AS policyKey,setting_value AS policyValue,description,use_yn AS useYn,updated_at AS updatedAt FROM bza_project_setting WHERE setting_key LIKE 'DOWNLOAD.%' ORDER BY setting_key
