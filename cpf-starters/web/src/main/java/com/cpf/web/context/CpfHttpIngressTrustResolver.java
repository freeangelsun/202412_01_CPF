package com.cpf.web.context;

import jakarta.servlet.http.HttpServletRequest;

/** Resolves whether an ingress request crossed a verified internal trust boundary. */
public interface CpfHttpIngressTrustResolver {
    String VERIFIED_INTERNAL_CALLER_ATTRIBUTE = "cpf.verified.internal.caller-system-code";

    Decision resolve(HttpServletRequest request);

    record Decision(CpfHttpIngressTrust trust, String verifiedCallerSystemCode) {
        public Decision {
            if (trust == null) trust = CpfHttpIngressTrust.UNTRUSTED_EXTERNAL;
        }
    }
}
