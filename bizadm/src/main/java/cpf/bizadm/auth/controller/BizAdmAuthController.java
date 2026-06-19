package cpf.bizadm.auth.controller;

import cpf.bizadm.auth.service.BizAdmAuthService;
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
 * BIZADM 업무 관리자 인증 API입니다.
 *
 * <p>이 API는 sample 패키지가 아닌 BIZADM 인증 기능 그룹에 속합니다. 로그인 이력과 refresh token은
 * bizadmDB에 저장되며, datasource가 비활성화된 환경에서는 명확한 서비스 사용 불가 오류를 반환합니다.</p>
 */
@RestController
@RequestMapping("/api/bizadm/auth")
@Tag(name = "BIZADM-Auth", description = "업무 관리자 로그인, refresh token, 현재 사용자, 로그인 이력 API")
public class BizAdmAuthController {
    private final BizAdmAuthService authService;

    public BizAdmAuthController(BizAdmAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @CpfTransaction(id = "BIZ02AUT0001", name = "BizAdmLogin")
    @Operation(summary = "업무 관리자 로그인", description = "BIZADM 전용 JWT access token과 DB hash 저장형 refresh token을 발급합니다.")
    public ResponseEntity<BizAdmAuthService.LoginResult> login(
            @RequestBody BizAdmAuthService.LoginRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(
                request,
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/refresh")
    @CpfTransaction(id = "BIZ02AUT0002", name = "BizAdmTokenRefresh")
    @Operation(summary = "업무 관리자 token 재발급", description = "원문 refresh token을 hash 비교한 뒤 BIZADM access token을 재발급합니다.")
    public ResponseEntity<BizAdmAuthService.LoginResult> refresh(@RequestBody BizAdmAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @CpfTransaction(id = "BIZ02AUT0003", name = "BizAdmLogout")
    @Operation(summary = "업무 관리자 로그아웃", description = "DB에 저장된 refresh token hash 상태를 폐기 처리합니다.")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) BizAdmAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @GetMapping("/me")
    @CpfTransaction(id = "BIZ01AUT0004", name = "BizAdmCurrentOperator")
    @Operation(summary = "현재 업무 관리자", description = "BIZADM access token의 loginDomain을 검증한 뒤 현재 업무 관리자 정보를 반환합니다.")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(authService.currentOperator(authorization));
    }

    @GetMapping("/login-history")
    @CpfTransaction(id = "BIZ01AUT0005", name = "BizAdmLoginHistory")
    @Operation(summary = "업무 관리자 로그인 이력", description = "성공/실패, 실패 사유, transactionGlobalId, moduleId, wasId, serverInstanceId를 포함해 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> loginHistories(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(authService.loginHistories(limit));
    }
}
