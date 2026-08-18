-- GENERATED from cpf-tools/db/canonical/platform-seed-currentization.json; DO NOT EDIT.
-- D-010 R124 policy=PRESERVE_CURRENT_NOOP.
-- Exact pre-upgrade customer state cannot be reconstructed safely; rollback must not reintroduce retired REF ownership or remove current EDU product rows.
USE cpfDB;
START TRANSACTION;
-- Intentionally no data mutation.
COMMIT;
