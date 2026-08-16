SELECT outcome,COUNT(*) FROM cpf_xa_recovery GROUP BY outcome ORDER BY outcome;
SELECT head_id,sequence_no,current_hash,version_no FROM cpf_tamper_audit_head;
SELECT COUNT(*) AS broken_chain FROM cpf_tamper_audit a LEFT JOIN cpf_tamper_audit p ON p.sequence_no=a.sequence_no-1 WHERE a.sequence_no>1 AND a.previous_hash<>p.current_hash;
SELECT COUNT(*) AS cpf_ref_tcc_reservation_exists FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='cpf_ref_tcc_reservation';
