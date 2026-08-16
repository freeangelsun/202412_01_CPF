package com.cpf.security.oidc;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** 현재 OIDC 인증을 CPF principal snapshot으로 읽는 개발자 편의 API입니다. */
public final class CpfOidcContext {
    private final CpfOidcPrincipalMapper mapper;
    public CpfOidcContext(CpfOidcPrincipalMapper mapper) { this.mapper = mapper; }
    public Optional<CpfOidcPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof OidcUser user)) return Optional.empty();
        return Optional.of(mapper.map(user));
    }
}
