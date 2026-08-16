-- Verify CPF R6 V105 approval runtime hardening
DECLARE v_count NUMBER; BEGIN
 SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='ADM_APPROVAL_EXECUTION' AND column_name IN ('LEASE_OWNER','LEASE_EXPIRES_AT','FENCE_TOKEN');
 IF v_count<>3 THEN RAISE_APPLICATION_ERROR(-20064,'approval execution lease/fence columns missing'); END IF;
 SELECT COUNT(*) INTO v_count FROM user_tables WHERE table_name='ADM_APPROVAL_CAPABILITY_NONCE';
 IF v_count<>1 THEN RAISE_APPLICATION_ERROR(-20065,'approval capability nonce table missing'); END IF;
 SELECT COUNT(*) INTO v_count FROM adm_approval_policy_lock;
 IF v_count<>64 THEN RAISE_APPLICATION_ERROR(-20066,'approval policy lock buckets expected 64'); END IF;
 SELECT COUNT(*) INTO v_count FROM user_triggers WHERE trigger_name IN ('TR_ADM_APPROVAL_POLICY_IMMUTABLE_U','TR_ADM_APPROVAL_POLICY_IMMUTABLE_D','TR_ADM_APPROVAL_POLICY_NO_OVERLAP') AND status='ENABLED';
 IF v_count<>3 THEN RAISE_APPLICATION_ERROR(-20067,'approval policy hardening triggers missing'); END IF;
END;
/
