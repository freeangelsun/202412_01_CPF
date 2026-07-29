SELECT position_code AS positionCode,position_name AS positionName,rank_order AS rankOrder,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
FROM bza_position ORDER BY rank_order,position_code OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
