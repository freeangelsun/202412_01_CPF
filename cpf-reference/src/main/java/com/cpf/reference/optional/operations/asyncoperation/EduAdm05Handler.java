package com.cpf.reference.optional.operations.asyncoperation;

import com.cpf.reference.optional.operations.config.EduAdmRedirectMetadata;

/**
 * EDU-ADM-05 is intentionally non-executable.
 * Central architecture decision: MERGE_EDU. Product behavior is owned by cpf-reference:merged-edu.
 * The legacy class name is retained only as source-level redirect metadata so historical
 * references fail closed instead of silently registering duplicate runtime behavior.
 */
public final class EduAdm05Handler {
    public static final EduAdmRedirectMetadata REDIRECT = new EduAdmRedirectMetadata(
            "EDU-ADM-05", "MERGE_EDU", "cpf-reference:merged-edu", "CPF_ADM_OPERATOR", false);

    private EduAdm05Handler() { }
}
