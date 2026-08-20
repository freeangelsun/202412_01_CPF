package com.cpf.backoffice.web.shared.security;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SPA가 mutation 전에 same-origin CSRF token을 초기화하는 BFF 전용 Endpoint입니다. */
@RestController
@RequestMapping("/api/v1/backoffice/security")
public final class BackofficeCsrfController {
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }
}
