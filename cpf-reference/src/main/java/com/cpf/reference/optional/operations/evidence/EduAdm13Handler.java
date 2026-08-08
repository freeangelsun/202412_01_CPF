package com.cpf.reference.optional.operations.evidence;

import com.cpf.reference.optional.operations.config.EduAdmRedirectMetadata;

/**
 * EDU-ADM-13 is intentionally non-executable.
 * Central architecture decision: PRODUCT_ADM. Product behavior is owned by cpf-admin.
 * The legacy class name is retained only as source-level redirect metadata so historical
 * references fail closed instead of silently registering duplicate runtime behavior.
 */
public final class EduAdm13Handler {
    public static final EduAdmRedirectMetadata REDIRECT = new EduAdmRedirectMetadata(
            "EDU-ADM-13", "PRODUCT_ADM", "cpf-admin", "CPF_ADM_OPERATOR", false);

    private EduAdm13Handler() { }
}
