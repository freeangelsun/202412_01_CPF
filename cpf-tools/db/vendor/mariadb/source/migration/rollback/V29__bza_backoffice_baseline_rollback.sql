-- PRE-GA R9 rollback note: V29 is the BZA baseline.
-- Baseline rollback must restore the pre-upgrade bzaDB backup rather than drop customer/operator data.
SELECT 'Restore pre-upgrade bzaDB backup for V29 baseline rollback' AS cpf_rollback_note;
