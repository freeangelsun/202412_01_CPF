package com.cpf.backoffice.online.auth.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.BackofficeOperatorRow;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.LoginHistoryWrite;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.LoginOperationState;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.RefreshTokenWrite;
import org.springframework.http.HttpStatus;
import com.cpf.backoffice.online.base.BackofficeBaseService;
import com.cpf.foundation.annotation.CpfService;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * MBW 로그인에서 인증 실패 기록과 성공 세션 발급의 Transaction 경계를 명시적으로 분리합니다.
 * 인증 실패는 401 응답과 무관하게 이력/실패횟수를 commit하고, 성공은 계정 갱신·이력·refresh session을
 * 하나의 Transaction으로 commit하여 부분 성공을 남기지 않습니다.
 */
@CpfService
public class BackofficeLoginTransactionService extends BackofficeBaseService {
    private static final String LOGIN_DOMAIN = "MBW";
    private final BackofficeAuthRepository repository;

    public BackofficeLoginTransactionService(BackofficeAuthRepository repository) {
        this.repository = repository;
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public void recordFailure(LoginFailureCommand command) {
        if (command.increaseFailCount() && command.adminUserId() != null) {
            repository.increaseLoginFailCount(command.adminUserId());
        }
        repository.insertLoginHistory(new LoginHistoryWrite(
                command.adminUserId(), LOGIN_DOMAIN, command.loginId(), "FAIL", command.reason(),
                command.clientIp(), command.userAgent(), command.transactionId(), command.systemCode(),
                command.application(), command.instanceId()));
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public LoginCommitResult commitSuccess(LoginSuccessCommand command) {
        boolean created = repository.insertLoginOperation(
                command.idempotencyKey(), command.operator().adminUserId(), command.operator().loginId(), command.requestHash());
        LoginOperationState state = repository.lockLoginOperation(command.idempotencyKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "로그인 operation 처리 상태를 확인할 수 없습니다."));
        if (state.adminUserId() != command.operator().adminUserId()
                || !state.loginId().equals(command.operator().loginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency-Key가 다른 업무 관리자 로그인에 이미 사용되었습니다.");
        }
        if (!state.requestHash().equals(command.requestHash())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "동일 Idempotency-Key가 다른 로그인 요청 payload에 재사용되었습니다.");
        }

        if (!created) {
            if ("SUCCESS".equals(state.status())) {
                if (state.resultExpiresAt() == null || !state.resultExpiresAt().isAfter(Instant.now())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "동일 로그인 요청의 replay 보존 시간이 만료되었습니다. 새 Idempotency-Key를 사용하세요.");
                }
                if (state.resultAccessTokenEnc() == null || state.resultRefreshTokenEnc() == null
                        || state.resultRefreshExpiresAt() == null) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                            "로그인 operation의 저장 결과가 불완전합니다. 운영 확인이 필요합니다.");
                }
                return new LoginCommitResult(
                        state.resultAccessTokenEnc(), state.resultRefreshTokenEnc(), state.resultRefreshExpiresAt(), true);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "이전 로그인 요청이 완료되지 않았습니다. idempotencyKey=" + command.idempotencyKey()
                            + ", status=" + state.status());
        }

        repository.markLoginSuccess(command.operator().adminUserId());
        if (command.upgradedPasswordHash() != null) {
            repository.updatePasswordHashIfUnchanged(command.operator().adminUserId(),
                    command.previousPasswordHash(), command.upgradedPasswordHash(), "MBW_PASSWORD_UPGRADE");
        }
        repository.insertLoginHistory(new LoginHistoryWrite(
                command.operator().adminUserId(), LOGIN_DOMAIN, command.operator().loginId(), "SUCCESS", null,
                command.clientIp(), command.userAgent(), command.transactionId(), command.systemCode(),
                command.application(), command.instanceId()));
        repository.insertRefreshToken(new RefreshTokenWrite(
                command.operator().adminUserId(), LOGIN_DOMAIN, command.refreshTokenHash(), command.transactionId(),
                command.idempotencyKey(), command.refreshExpireAt()));
        repository.markLoginOperationSuccess(
                command.idempotencyKey(), command.resultAccessTokenEnc(), command.resultRefreshTokenEnc(),
                command.refreshExpireAt(), command.resultExpireAt());
        return new LoginCommitResult(
                command.resultAccessTokenEnc(), command.resultRefreshTokenEnc(), command.refreshExpireAt(), false);
    }


    public record LoginFailureCommand(
            Long adminUserId,
            String loginId,
            String reason,
            String clientIp,
            String userAgent,
            boolean increaseFailCount,
            String transactionId,
            String systemCode,
            String application,
            String instanceId) {
    }

    public record LoginSuccessCommand(
            String idempotencyKey,
            String requestHash,
            BackofficeOperatorRow operator,
            String previousPasswordHash,
            String upgradedPasswordHash,
            String refreshTokenHash,
            Instant refreshExpireAt,
            String resultAccessTokenEnc,
            String resultRefreshTokenEnc,
            Instant resultExpireAt,
            String clientIp,
            String userAgent,
            String transactionId,
            String systemCode,
            String application,
            String instanceId) {
    }

    public record LoginCommitResult(
            String resultAccessTokenEnc,
            String resultRefreshTokenEnc,
            Instant refreshExpireAt,
            boolean replayed) {
    }
}
