package com.cpf.starter.security.oidc;

import java.util.LinkedHashSet;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** Provider SDK 없이 표준 OIDC user를 CPF 권한 매핑과 결합합니다. */
public final class CpfOidcUserService extends OidcUserService {
    private final CpfOidcPrincipalMapper mapper;
    public CpfOidcUserService(CpfOidcPrincipalMapper mapper) { this.mapper = mapper; }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = super.loadUser(userRequest);
        CpfOidcPrincipal principal = mapper.map(user);
        var authorities = new LinkedHashSet<>(user.getAuthorities());
        principal.authorities().stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
        return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo(), "sub");
    }
}
