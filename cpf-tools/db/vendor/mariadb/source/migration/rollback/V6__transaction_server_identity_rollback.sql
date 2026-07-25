-- PRE-GA R9 rollback note: V6 fixed EXS/BIZADM ownership was removed.
-- Server identity columns are retained for backward/forward trace compatibility; no destructive rollback is performed.
SELECT 'V6 canonical repair rollback is intentionally non-destructive' AS cpf_rollback_note;
