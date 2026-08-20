UPDATE mbw_attachment SET scan_status=:scanStatus,data_classification=:classification,retention_until=:retentionUntil,
       quarantine_yn=:quarantineYn,use_yn=:useYn,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
 WHERE attachment_id=:attachmentId
