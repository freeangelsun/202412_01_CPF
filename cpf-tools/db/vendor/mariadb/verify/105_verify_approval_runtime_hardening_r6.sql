-- Verify CPF R6 V105 approval runtime hardening
DELIMITER $$
DROP PROCEDURE IF EXISTS CPF_VERIFY_APPROVAL_V105$$
CREATE PROCEDURE CPF_VERIFY_APPROVAL_V105()
BEGIN
  DECLARE v_count INT DEFAULT 0;
  SELECT COUNT(*) INTO v_count FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='adm_approval_execution' AND column_name IN ('lease_owner','lease_expires_at','fence_token');
  IF v_count<>3 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='approval execution lease/fence columns missing'; END IF;
  SELECT COUNT(*) INTO v_count FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='adm_approval_capability_nonce';
  IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='approval capability nonce table missing'; END IF;
  SELECT COUNT(*) INTO v_count FROM adm_approval_policy_lock;
  IF v_count<>64 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='approval policy lock buckets expected 64'; END IF;
  SELECT COUNT(*) INTO v_count FROM information_schema.triggers WHERE trigger_schema=DATABASE() AND trigger_name IN ('tr_adm_approval_policy_immutable_u','tr_adm_approval_policy_immutable_d','tr_adm_approval_policy_no_overlap');
  IF v_count<>3 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='approval policy hardening triggers missing'; END IF;
END$$
CALL CPF_VERIFY_APPROVAL_V105()$$
DROP PROCEDURE CPF_VERIFY_APPROVAL_V105$$
DELIMITER ;
