package com.cpf.backoffice.online.auth.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.auth.service.BackofficeAuthService;
import com.cpf.backoffice.online.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;

/**
 * MBW 업무 관리자 인증 API입니다.
 *
 * <p>이 API는 sample 패키지가 아닌 MBW 인증 기능 그룹에 속합니다. 로그인 이력과 refresh token은
 * MBW_DB에 저장되며, datasource가 비활성화된 환경에서는 명확한 서비스 사용 불가 오류를 반환합니다.</p>
 */
@CpfController
@RequestMapping("/api/v1/backoffice/auth")
@Tag(name = "MBW-Auth", description = "업무 관리자 로그인, refresh token, 현재 사용자, 로그인 이력 API")
public class BackofficeAuthController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final BackofficeAuthService authService;

    public BackofficeAuthController(BackofficeAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")    @Operation(operationId = "MBW_AUTH_LOGIN", summary = "업무 관리자 로그인",
            description = "Canonical Operation ID는 MBW_AUTH_LOGIN으로 고정되고 Idempotency-Key가 필수입니다. 성공 흐름은 계정 갱신·성공 이력·Refresh Session을 "
                    + "하나의 MBW_DB Transaction으로 commit합니다. 응답 유실 시 동일 Idempotency-Key를 재사용하면 "
                    + "기존 Refresh Session을 폐기하고 하나의 활성 Session으로 회전합니다. 인증 실패 횟수와 "
                    + "실패 이력은 별도 Transaction으로 commit됩니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_LOGIN", name = "업무 관리자 로그인", description = "업무 관리자 로그인 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 결과불명 재시도의 안전한 Session 회전"),
            @ApiResponse(responseCode = "400", description = "Idempotency-Key/loginId/password 누락"),
            @ApiResponse(responseCode = "401", description = "계정 상태 또는 인증 실패"),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key가 다른 사용자 요청에 재사용됨"),
            @ApiResponse(responseCode = "503", description = "MBW_DB 장애로 성공/실패 이력을 안전하게 확정할 수 없음")
    })
    public ResponseEntity<BackofficeAuthService.LoginResult> login(
            @RequestBody BackofficeAuthService.LoginRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(
                request.withIdempotencyKey(idempotencyKey),
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/refresh")    @Operation(operationId = "MBW_AUTH_REFRESH", summary = "업무 관리자 token 재발급", description = "원문 refresh token을 hash 비교한 뒤 MBW access token을 재발급합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_REFRESH", name = "업무 관리자 token 재발급", description = "업무 관리자 token 재발급 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficeAuthService.LoginResult> refresh(
            @RequestBody BackofficeAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")    @Operation(operationId = "MBW_AUTH_LOGOUT", summary = "업무 관리자 로그아웃", description = "DB에 저장된 refresh token hash 상태를 폐기 처리합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_LOGOUT", name = "업무 관리자 로그아웃", description = "업무 관리자 로그아웃 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficeLogoutResponse> logout(
            @RequestBody(required = false) BackofficeAuthService.RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @GetMapping("/me")    @Operation(operationId = "MBW_AUTH_ME", summary = "현재 업무 관리자", description = "MBW access token의 loginDomain을 검증한 뒤 현재 업무 관리자 정보를 반환합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_ME", name = "현재 업무 관리자", description = "현재 업무 관리자 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficeCurrentOperatorResponse> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(authService.currentOperator(authorization));
    }

    @GetMapping("/login-history")    @Operation(operationId = "MBW_AUTH_LOGIN_HISTORIES", summary = "업무 관리자 로그인 이력", description = "성공/실패, 실패 사유, transactionId, systemCode, application, instanceId를 포함해 조회합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_LOGIN_HISTORIES", name = "업무 관리자 로그인 이력", description = "업무 관리자 로그인 이력 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<BackofficeLoginHistoryResponse>> loginHistories(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(authService.loginHistories(authorization, limit));
    }

    @GetMapping("/sessions")    @Operation(operationId = "MBW_AUTH_SESSIONS", summary = "현재 사용자 세션 조회",
            description = "refresh token 원문과 hash를 제외한 발급 거래·만료·폐기 상태를 조회합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_SESSIONS", name = "현재 사용자 세션 조회", description = "현재 사용자 세션 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<BackofficeSessionResponse>> sessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(authService.sessions(authorization, limit));
    }

    @PostMapping("/sessions/{sessionId}/revoke")    @Operation(operationId = "MBW_AUTH_REVOKE_SESSION", summary = "현재 사용자 세션 폐기",
            description = "현재 access token 사용자 소유의 활성 refresh session만 감사 사유와 함께 폐기합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_REVOKE_SESSION", name = "현재 사용자 세션 폐기", description = "현재 사용자 세션 폐기 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficeSessionRevokeResponse> revokeSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @org.springframework.web.bind.annotation.PathVariable long sessionId,
            @RequestParam String reason) {
        return ResponseEntity.ok(authService.revokeSession(authorization, sessionId, reason));
    }

    @PostMapping("/password/change")    @Operation(operationId = "MBW_AUTH_CHANGE_PASSWORD", summary = "업무 관리자 본인 비밀번호 변경",
            description = "현재 비밀번호를 검증하고 CPF 공통 hash 형식으로 변경한 뒤 기존 refresh token을 모두 폐기합니다.")
    @CpfOnlineTransaction(operationId = "MBW_AUTH_CHANGE_PASSWORD", name = "업무 관리자 본인 비밀번호 변경", description = "업무 관리자 본인 비밀번호 변경 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficePasswordChangeResponse> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody BackofficeAuthService.PasswordChangeRequest request) {
        return ResponseEntity.ok(authService.changePassword(authorization, request));
    }
}
