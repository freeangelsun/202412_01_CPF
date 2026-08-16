package com.cpf.security.oidc;

import com.cpf.core.api.context.CpfContext;
import java.time.Instant;
import java.util.Objects;

/** Maps verified OIDC identity into the topology-neutral CPF Identity/Tenant Context. */
public final class CpfOidcContextBridge {
    public CpfContext apply(CpfContext base,CpfOidcPrincipal principal,Instant authenticatedAt){
        Objects.requireNonNull(base,"base");Objects.requireNonNull(principal,"principal");
        var identity=new CpfContext.CpfIdentityContext(principal.userId(),principal.userId(),CpfContext.CpfPrincipalType.USER,
                null,null,"OIDC",Objects.requireNonNull(authenticatedAt,"authenticatedAt"));
        var tenant=principal.tenantId()==null?base.tenant():new CpfContext.CpfTenantContext(principal.tenantId());
        return base.withIdentityAndTenant(identity,tenant);
    }
}
