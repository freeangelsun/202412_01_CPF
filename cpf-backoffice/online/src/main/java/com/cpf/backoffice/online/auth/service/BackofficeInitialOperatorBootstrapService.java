package com.cpf.backoffice.online.auth.service;

import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.security.api.password.CpfPasswordEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Fresh MBW 환경의 최초 운영자만 생성하는 one-time Product contract owner다.
 *
 * <p>정상 운영자 생성은 이 service를 호출하지 않고 maker/checker approval-token runner를 사용한다.
 * 현재 MBW 감사 체인의 database lock을 transaction boundary로 사용해 같은 DB를 쓰는 다중
 * instance도 둘 이상의 최초 운영자를 만들지 못하게 한다.</p>
 */
@CpfService
// @CpfTransactional 이 있는 Bean 은 CGLIB proxy 로 감싸진다. final class 는 subclass 할 수 없어
// "Cannot subclass final class" 로 기동이 실패한다. 또 @CpfService 는 Domain Base Class 상속을
// 요구한다. 통합 Runtime 에서는 이 Bean 이 조립되지 않아 두 계약 위반이 모두 드러나지 않았고,
// MBW 단독 기동에서 처음 재현됐다.
public class BackofficeInitialOperatorBootstrapService
        extends com.cpf.backoffice.online.base.BackofficeBaseService {
    static final String INITIAL_OPERATION_ID = "MBW-INITIAL-OPERATOR";

    private final BackofficeAuthRepository authRepository;
    private final BackofficeBusinessAuditService auditService;
    private final CpfPasswordEncoder passwordEncoder;

    public BackofficeInitialOperatorBootstrapService(
            BackofficeAuthRepository authRepository,
            BackofficeBusinessAuditService auditService,
            CpfPasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 최초 계정이 없을 때만 secret을 소비한다. 이미 계정이 있으면 secret이 다시 전달돼도
     * password/role을 읽거나 덮어쓰지 않고 상태만 반환한다.
     */
    public Result bootstrap(Command command) {
        return auditService.withAuditChainLock(() -> bootstrapLocked(command));
    }

    @CpfTransactional(transactionManager = "MBW_TRANSACTION_MANAGER")
    private Result bootstrapLocked(Command command) {
        BackofficeAuthRepository.BootstrapResult prior = authRepository
                .findBootstrapOperation(INITIAL_OPERATION_ID)
                .orElse(null);
        if (prior != null) {
            command.verifyMatchingLoginIfSupplied(prior.loginId());
            return Result.alreadyBootstrapped(prior.adminUserId(), prior.loginId());
        }
        if (authRepository.hasAnyOperator()) {
            return Result.existingOperator();
        }

        command.requireFreshBootstrapInput();
        char[] password = command.password();
        try {
            BackofficeBootstrapRunner.requireStrongPassword(command.loginId(), password);
            Instant now = Instant.now();
            BackofficeAuthRepository.BootstrapResult created = authRepository.bootstrapOperator(
                    command.loginId(), command.operatorName(), passwordEncoder.encode(password), command.roleCode(),
                    INITIAL_OPERATION_ID, now.plusSeconds(command.passwordExpiryDays() * 24L * 60L * 60L));
            auditService.record(
                    "CPF_BOOTSTRAP",
                    "INITIAL_OPERATOR_BOOTSTRAP",
                    "MBW_OPERATOR",
                    created.loginId(),
                    "initial operator bootstrap",
                    null,
                    Map.of(
                            "result", "CREATED",
                            "source", "CPF_BOOTSTRAP",
                            "environment", command.environmentCode(),
                            "profiles", command.activeProfiles(),
                            "instanceId", command.instanceId(),
                            "bootstrapSecretProvided", true));
            return Result.created(created.adminUserId(), created.loginId());
        } finally {
            if (password != null) Arrays.fill(password, '\0');
        }
    }

    public record Command(
            String loginId,
            String operatorName,
            String roleCode,
            int passwordExpiryDays,
            char[] password,
            String environmentCode,
            List<String> activeProfiles,
            String instanceId) {

        void requireFreshBootstrapInput() {
            require(loginId, "loginId");
            require(operatorName, "operatorName");
            require(roleCode, "roleCode");
            require(environmentCode, "environmentCode");
            require(instanceId, "instanceId");
            if (activeProfiles == null) throw new IllegalStateException("MBW_INITIAL_OPERATOR_PROFILES_REQUIRED");
            if (password == null || password.length == 0) {
                throw new IllegalStateException("MBW_INITIAL_OPERATOR_BOOTSTRAP_SECRET_REQUIRED");
            }
            if (passwordExpiryDays < 1 || passwordExpiryDays > 366) {
                throw new IllegalStateException("MBW_INITIAL_OPERATOR_PASSWORD_EXPIRY_DAYS_INVALID");
            }
        }

        void verifyMatchingLoginIfSupplied(String actualLoginId) {
            if (loginId != null && !loginId.isBlank() && !loginId.trim().equals(actualLoginId)) {
                throw new SecurityException("MBW_INITIAL_OPERATOR_LOGIN_MISMATCH");
            }
        }

        private static void require(String value, String name) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("MBW_INITIAL_OPERATOR_PROPERTY_REQUIRED:" + name);
            }
        }
    }

    public record Result(Status status, Long adminUserId, String loginId) {
        static Result created(long id, String login) {
            return new Result(Status.CREATED, id, login);
        }

        static Result alreadyBootstrapped(long id, String login) {
            return new Result(Status.ALREADY_BOOTSTRAPPED, id, login);
        }

        static Result existingOperator() {
            return new Result(Status.EXISTING_OPERATOR, null, null);
        }
    }

    public enum Status {
        CREATED,
        ALREADY_BOOTSTRAPPED,
        EXISTING_OPERATOR
    }
}
