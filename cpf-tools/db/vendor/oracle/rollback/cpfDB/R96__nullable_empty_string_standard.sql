-- GENERATED FILE. DO NOT EDIT VENDOR SQL DIRECTLY.
-- Source: cpf-tools/db/metadata/platform-nullable-empty-string-repair.json + canonical schemaVersion 46.
-- Repair: CPF-MIGRATION-096-NULLABLE-EMPTY-STRING; historical migrations remain immutable.

-- Oracle treats the empty string as NULL, so the historical NOT NULL DEFAULT empty-string state is unrepresentable.
-- Fail closed instead of coercing NULL business values to a fabricated sentinel.
BEGIN
  RAISE_APPLICATION_ERROR(-20096, 'R96 cannot restore Oracle empty-string NOT NULL semantics; restore the pre-V96 backup instead');
END;
/
