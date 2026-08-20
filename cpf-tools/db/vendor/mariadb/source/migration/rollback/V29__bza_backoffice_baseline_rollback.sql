-- PRE-GA R9 rollback note: V29 is the MBW baseline.
-- Baseline rollback must restore the pre-upgrade backofficeDB backup rather than drop customer/operator data.
SELECT 'Restore pre-upgrade backofficeDB backup for V29 baseline rollback' AS cpf_rollback_note;
