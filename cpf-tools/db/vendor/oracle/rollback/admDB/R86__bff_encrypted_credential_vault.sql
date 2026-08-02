DECLARE
    active_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO active_count FROM cpf_bff_credential_vault WHERE refresh_expires_at > SYSTIMESTAMP;
    IF active_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20086, 'R86 denied: active BFF credentials exist');
    END IF;
END;
/
DROP TABLE cpf_bff_credential_vault PURGE;
