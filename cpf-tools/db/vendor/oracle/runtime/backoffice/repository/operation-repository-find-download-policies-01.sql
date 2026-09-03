SELECT setting_key AS policyKey,setting_value AS policyValue,description,use_yn AS useYn,updated_at AS updatedAt FROM MBW_PROJECT_SETTING WHERE setting_key LIKE 'DOWNLOAD.%' ORDER BY setting_key
