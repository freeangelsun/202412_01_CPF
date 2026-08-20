package com.cpf.backoffice.web.shared.protocol;

import java.util.Set;

/** Public HTTP wire names only. No CPF Java dependency is allowed in this application. */
public final class CanonicalTransactionHeaders {
    public static final String TRANSACTION_ID = "X-Transaction-Id";
    public static final String ORIGINAL_SYSTEM_CODE = "X-Original-System-Code";
    public static final String SYSTEM_CODE = "X-System-Code";
    public static final String CALLER_SYSTEM_CODE = "X-Caller-System-Code";
    public static final String TARGET_SYSTEM_CODE = "X-Target-System-Code";
    public static final String TARGET_OPERATION_ID = "X-Target-Operation-Id";
    public static final String CALLER_CHANNEL = "X-Caller-Channel";
    public static final Set<String> BROWSER_FORBIDDEN = Set.of(
            TRANSACTION_ID, ORIGINAL_SYSTEM_CODE, SYSTEM_CODE, CALLER_SYSTEM_CODE, TARGET_SYSTEM_CODE, TARGET_OPERATION_ID);
    private CanonicalTransactionHeaders() {}
}
