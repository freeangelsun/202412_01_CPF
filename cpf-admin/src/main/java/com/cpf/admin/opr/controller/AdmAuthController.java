package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmLoginRequest;
import com.cpf.admin.opr.dto.AdmLoginResponse;
import com.cpf.admin.opr.dto.AdmCurrentSessionResponse;
import com.cpf.admin.opr.dto.AdmLogoutResponse;
import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.service.AdmOperatorService;
import com.cpf.admin.opr.service.AdmSessionService;
import com.cpf.admin.opr.service.AdmSecurityOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adm/api/auth")
@Tag(name = "ADM-OPR Auth", description = "ADM 운영자 인증과 세션 API")
public class AdmAuthController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmOperatorService operatorService;
    private final AdmSessionService sessionService;
    private final AdmSecurityOperationService securityService;

    public AdmAuthController(AdmOperatorService operatorService, AdmSessionService sessionService,
                             AdmSecurityOperationService securityService) {
        this.operatorService = operatorService;
        this.sessionService = sessionService;
        this.securityService = securityService;
    }

    @PostMapping("/login")    @Operation(operationId = "admAuthLogin", summary = "ADM 로그인", description = "운영자를 인증하고 Bearer 토큰 세션을 발급합니다.")
    public ResponseEntity<AdmLoginResponse> login(@RequestBody AdmLoginRequest request) {
        AdmOperator operator = operatorService.authenticate(request);
        securityService.requireMfaForLogin(operator.operatorId(), request.otpCode());
        List<AdmMenu> menus = operatorService.findMenusForRoles(operator.roleIds());
        return ResponseEntity.ok(sessionService.issue(operator, menus, operatorService.findButtonIdsForRoles(operator.roleIds())));
    }

    @GetMapping("/me")    @Operation(operationId = "admAuthMe", summary = "현재 운영자 조회", description = "현재 세션의 운영자와 권한 메뉴를 조회합니다.")
    public ResponseEntity<AdmCurrentSessionResponse> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = bearerToken(authorization);
        var session = sessionService.findValidSession(token)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "유효하지 않은 ADM 세션입니다."));
        return ResponseEntity.ok(new AdmCurrentSessionResponse(session.operatorId(), session.roleIds(),
                session.passwordChangeRequired(), operatorService.findMenusForRoles(session.roleIds()),
                operatorService.findButtonIdsForRoles(session.roleIds())));
    }

    @PostMapping("/logout")    @Operation(operationId = "admAuthLogout", summary = "ADM 로그아웃", description = "현재 Bearer 토큰 세션을 폐기합니다.")
    public ResponseEntity<AdmLogoutResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        sessionService.revoke(bearerToken(authorization));
        return ResponseEntity.ok(new AdmLogoutResponse(true));
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
