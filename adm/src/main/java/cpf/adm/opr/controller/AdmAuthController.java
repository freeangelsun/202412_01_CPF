package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmLoginRequest;
import cpf.adm.opr.dto.AdmLoginResponse;
import cpf.adm.opr.dto.AdmMenu;
import cpf.adm.opr.dto.AdmOperator;
import cpf.adm.opr.service.AdmOperatorService;
import cpf.adm.opr.service.AdmSessionService;
import cpf.pfw.common.logging.CpfTransaction;
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
import java.util.Map;

@RestController
@RequestMapping("/adm/api/auth")
@Tag(name = "ADM-OPR Auth", description = "ADM 운영자 인증과 세션 API")
public class AdmAuthController {
    private final AdmOperatorService operatorService;
    private final AdmSessionService sessionService;

    public AdmAuthController(AdmOperatorService operatorService, AdmSessionService sessionService) {
        this.operatorService = operatorService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    @CpfTransaction(id = "ADM06OPR0040", name = "ADMLogin")
    @Operation(operationId = "admAuthLogin", summary = "ADM 로그인", description = "운영자를 인증하고 Bearer 토큰 세션을 발급합니다.")
    public ResponseEntity<AdmLoginResponse> login(@RequestBody AdmLoginRequest request) {
        AdmOperator operator = operatorService.authenticate(request);
        List<AdmMenu> menus = operatorService.findMenusForRoles(operator.roleIds());
        return ResponseEntity.ok(sessionService.issue(operator, menus));
    }

    @GetMapping("/me")
    @CpfTransaction(id = "ADM01OPR0041", name = "ADMCurrentOperator")
    @Operation(operationId = "admAuthMe", summary = "현재 운영자 조회", description = "현재 세션의 운영자와 권한 메뉴를 조회합니다.")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = bearerToken(authorization);
        return sessionService.findValidSession(token)
                .map(session -> ResponseEntity.ok(Map.<String, Object>of(
                        "operatorId", session.operatorId(),
                        "roleIds", session.roleIds(),
                        "passwordChangeRequired", session.passwordChangeRequired(),
                        "menus", operatorService.findMenusForRoles(session.roleIds()))))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("message", "유효하지 않은 ADM 세션입니다.")));
    }

    @PostMapping("/logout")
    @CpfTransaction(id = "ADM06OPR0042", name = "ADMLogout")
    @Operation(operationId = "admAuthLogout", summary = "ADM 로그아웃", description = "현재 Bearer 토큰 세션을 폐기합니다.")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        sessionService.revoke(bearerToken(authorization));
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
