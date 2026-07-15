package cpf.bza.auth.controller;

import cpf.bza.auth.service.BzaAuthService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
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
 * BZA 업무 관리자 인증 API입니다.
 *
 * <p>이 API는 sample 패키지가 아닌 BZA 인증 기능 그룹에 속합니다. 로그인 이력과 refresh token은
 * bzaDB에 저장되며, datasource가 비활성화된 환경에서는 명확한 서비스 사용 불가 오류를 반환합니다.</p>
 */
@RestController
@RequestMapping("/api/bza/auth")
@Tag(name = "BZA-Auth", description = "업무 관리자 로그인, refresh token, 현재 사용자, 로그인 이력 API")
public class BzaAuthController {
    private final BzaAuthService authService;

    public BzaAuthController(BzaAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @CpfOnlineTransaction(id = "OBZA-AUT-02-0001", name = "BzaLogin")
    @Operation(operationId = "bzaAuthLogin", summary = "업무 관리자 로그인", description = "BZA 전용 JWT access token과 DB hash 저장형 refresh token을 발급합니다.")
    public ResponseEntity<BzaAuthService.LoginResult> login(
            @RequestBody BzaAuthService.LoginRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(
                request,
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/refresh")
    @CpfOnlineTransaction(id = "OBZA-AUT-02-0002", name = "BzaTokenRefresh")
    @Operation(operationId = "bzaAuthRefresh", summary = "업무 관리자 token 재발급", description = "원문 refresh token을 hash 비교한 뒤 BZA access token을 재발급합니다.")
    public ResponseEntity<BzaAuthService.LoginResult> refresh(@RequestBody BzaAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @CpfOnlineTransaction(id = "OBZA-AUT-02-0003", name = "BzaLogout")
    @Operation(operationId = "bzaAuthLogout", summary = "업무 관리자 로그아웃", description = "DB에 저장된 refresh token hash 상태를 폐기 처리합니다.")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) BzaAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @GetMapping("/me")
    @CpfOnlineTransaction(id = "OBZA-AUT-01-0004", name = "BzaCurrentOperator")
    @Operation(operationId = "bzaAuthMe", summary = "현재 업무 관리자", description = "BZA access token의 loginDomain을 검증한 뒤 현재 업무 관리자 정보를 반환합니다.")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(authService.currentOperator(authorization));
    }

    @GetMapping("/login-history")
    @CpfOnlineTransaction(id = "OBZA-AUT-01-0005", name = "BzaLoginHistory")
    @Operation(operationId = "bzaAuthLoginHistories", summary = "업무 관리자 로그인 이력", description = "성공/실패, 실패 사유, transactionGlobalId, moduleId, wasId, serverInstanceId를 포함해 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> loginHistories(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(authService.loginHistories(authorization, limit));
    }

    @GetMapping("/sessions")
    @CpfOnlineTransaction(id = "OBZA-AUT-01-0007", name = "BzaSessionList")
    @Operation(operationId = "bzaAuthSessions", summary = "현재 사용자 세션 조회",
            description = "refresh token 원문과 hash를 제외한 발급 거래·만료·폐기 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> sessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(authService.sessions(authorization, limit));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @CpfOnlineTransaction(id = "OBZA-AUT-04-0008", name = "BzaSessionRevoke")
    @Operation(operationId = "bzaAuthRevokeSession", summary = "현재 사용자 세션 폐기",
            description = "현재 access token 사용자 소유의 활성 refresh session만 감사 사유와 함께 폐기합니다.")
    public ResponseEntity<Map<String, Object>> revokeSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @org.springframework.web.bind.annotation.PathVariable long sessionId,
            @RequestParam String reason) {
        return ResponseEntity.ok(authService.revokeSession(authorization, sessionId, reason));
    }

    @PostMapping("/password/change")
    @CpfOnlineTransaction(id = "OBZA-AUT-03-0006", name = "BzaPasswordChange")
    @Operation(operationId = "bzaAuthChangePassword", summary = "업무 관리자 본인 비밀번호 변경",
            description = "현재 비밀번호를 검증하고 PFW 공통 hash 형식으로 변경한 뒤 기존 refresh token을 모두 폐기합니다.")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody BzaAuthService.PasswordChangeRequest request) {
        return ResponseEntity.ok(authService.changePassword(authorization, request));
    }
}
