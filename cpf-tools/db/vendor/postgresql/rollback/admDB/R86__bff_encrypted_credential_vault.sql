DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cpf_bff_credential_vault WHERE refresh_expires_at > CURRENT_TIMESTAMP) THEN
        RAISE EXCEPTION 'R86 denied: active BFF credentials exist';
    END IF;
END $$;
DROP TABLE cpf_bff_credential_vault;
