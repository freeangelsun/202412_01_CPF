package com.cpf.security.resourceserver;

import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

final class CpfAudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final Set<String> audiences;
    CpfAudienceValidator(Set<String> audiences) { this.audiences = Set.copyOf(audiences); }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (audiences.isEmpty() || token.getAudience().stream().anyMatch(audiences::contains)) return OAuth2TokenValidatorResult.success();
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "CPF JWT audience mismatch", null));
    }
}
