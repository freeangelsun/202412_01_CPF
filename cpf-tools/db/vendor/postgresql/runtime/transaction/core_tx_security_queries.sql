SELECT transaction_id,attempt_id,outcome,fencing_token,updated_at FROM cpf_xa_recovery WHERE outcome IN ('ACTIVE','PREPARED','IN_DOUBT','HEURISTIC','UNKNOWN') ORDER BY updated_at;
SELECT sequence_no,transaction_id,actor_id,action_code,payload_hash,previous_hash,current_hash,key_id,key_version,algorithm,certificate_id,occurred_at FROM cpf_tamper_audit WHERE sequence_no>=? ORDER BY sequence_no FETCH FIRST ? ROWS ONLY;
-- TCC recovery candidates
SELECT transaction_id,branch_id,state,deadline_at,fencing_token FROM cpf_ref_tcc_reservation WHERE state IN ('TRYING','TRIED','UNKNOWN','MANUAL_REVIEW') ORDER BY updated_at;
