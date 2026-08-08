package com.cpf.reference.optional.operations.partialrecovery;

import com.cpf.reference.optional.operations.config.EduAdmRedirectMetadata;

/**
 * EDU-ADM-06 is intentionally non-executable.
 * Central architecture decision: MERGE_EDU. Product behavior is owned by cpf-reference:merged-edu.
 * The legacy class name is retained only as source-level redirect metadata so historical
 * references fail closed instead of silently registering duplicate runtime behavior.
 */
public final class EduAdm06Handler {
    public static final EduAdmRedirectMetadata REDIRECT = new EduAdmRedirectMetadata(
            "EDU-ADM-06", "MERGE_EDU", "cpf-reference:merged-edu", "CPF_ADM_OPERATOR", false);

    private EduAdm06Handler() { }
}
