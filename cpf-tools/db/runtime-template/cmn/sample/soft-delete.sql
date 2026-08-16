UPDATE cmn_sample_item SET deleted_yn='Y',status_code='INACTIVE',version_no=version_no+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE sample_item_id=? AND version_no=? AND deleted_yn='N'
