package cpf.mbr.bse.controller;

import cpf.mbr.bse.service.MbrAuthService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MBR 회원 인증 API입니다.
 */
@RestController
@RequestMapping("/mbr/auth")
@Tag(name = "MBR-Auth", description = "회원 로그인, refresh token, 현재 회원, 로그인 이력 API")
public class MbrAuthController {
    private final MbrAuthService authService;

    public MbrAuthController(MbrAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @CpfTransaction(id = "MBR02AUT0001", name = "MBRMemberLogin")
    @Operation(summary = "회원 로그인", description = "MBR 전용 JWT access token과 hash 저장형 refresh token을 발급합니다.")
    public ResponseEntity<MbrAuthService.LoginResult> login(
            @RequestBody MbrAuthService.LoginRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, servletRequest.getRemoteAddr(), servletRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/refresh")
    @CpfTransaction(id = "MBR02AUT0002", name = "MBRTokenRefresh")
    @Operation(summary = "회원 token 재발급", description = "회원 refresh token을 hash로 검증한 뒤 access token을 재발급합니다.")
    public ResponseEntity<MbrAuthService.LoginResult> refresh(@RequestBody MbrAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @CpfTransaction(id = "MBR02AUT0003", name = "MBRMemberLogout")
    @Operation(summary = "회원 로그아웃", description = "refresh token hash 상태를 폐기 처리합니다.")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) MbrAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @GetMapping("/me")
    @CpfTransaction(id = "MBR01AUT0004", name = "MBRCurrentMember")
    @Operation(summary = "현재 회원", description = "MBR access token의 loginDomain을 검증한 뒤 현재 회원 정보를 반환합니다.")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(authService.currentMember(authorization));
    }

    @GetMapping("/login-history")
    @CpfTransaction(id = "MBR01AUT0005", name = "MBRLoginHistory")
    @Operation(summary = "회원 로그인 이력", description = "성공/실패, 실패 사유, transactionGlobalId, moduleId, wasId, serverInstanceId를 포함해 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> loginHistories(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(authService.loginHistories(limit));
    }
}
