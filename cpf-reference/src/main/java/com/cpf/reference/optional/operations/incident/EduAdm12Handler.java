package com.cpf.reference.optional.operations.incident;

import com.cpf.reference.optional.operations.config.EduAdmRedirectMetadata;

/**
 * EDU-ADM-12 is intentionally non-executable.
 * Central architecture decision: PRODUCT_ADM. Product behavior is owned by cpf-admin.
 * The legacy class name is retained only as source-level redirect metadata so historical
 * references fail closed instead of silently registering duplicate runtime behavior.
 */
public final class EduAdm12Handler {
    public static final EduAdmRedirectMetadata REDIRECT = new EduAdmRedirectMetadata(
            "EDU-ADM-12", "PRODUCT_ADM", "cpf-admin", "CPF_ADM_OPERATOR", false);

    private EduAdm12Handler() { }
}
