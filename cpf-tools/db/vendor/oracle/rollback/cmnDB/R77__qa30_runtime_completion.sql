-- CPF_LOGICAL_DATABASE=cmnDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.

-- CPF_FORWARD_RECOVERY_ONLY: optional-text relaxation is intentionally not reversed; use backup/PITR + checksum-locked forward recovery.
