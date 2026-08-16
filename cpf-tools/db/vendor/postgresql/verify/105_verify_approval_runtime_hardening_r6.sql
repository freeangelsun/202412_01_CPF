-- Verify CPF R6 V105 approval runtime hardening
DO $$
DECLARE v_count INTEGER;
BEGIN
 SELECT COUNT(*) INTO v_count FROM information_schema.columns WHERE table_name='adm_approval_execution' AND column_name IN ('lease_owner','lease_expires_at','fence_token');
 IF v_count<>3 THEN RAISE EXCEPTION 'approval execution lease/fence columns missing'; END IF;
 SELECT COUNT(*) INTO v_count FROM information_schema.tables WHERE table_name='adm_approval_capability_nonce';
 IF v_count<>1 THEN RAISE EXCEPTION 'approval capability nonce table missing'; END IF;
 SELECT COUNT(*) INTO v_count FROM adm_approval_policy_lock;
 IF v_count<>64 THEN RAISE EXCEPTION 'approval policy lock buckets expected 64, got %',v_count; END IF;
 SELECT COUNT(*) INTO v_count FROM pg_trigger WHERE tgname IN ('tr_adm_approval_policy_immutable_u','tr_adm_approval_policy_immutable_d','tr_adm_approval_policy_no_overlap') AND NOT tgisinternal;
 IF v_count<>3 THEN RAISE EXCEPTION 'approval policy hardening triggers missing'; END IF;
END $$;
