UPDATE mbw_audit_chain_lock SET current_hash=:hash,last_audit_id=:auditId,version_no=version_no+1,updated_by=:actor,updated_at=CURRENT_TIMESTAMP(3) WHERE chain_id=:id
