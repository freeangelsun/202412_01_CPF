package cpf.pfw.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.header.CpfInboundHeaderValidator;
import cpf.pfw.common.execution.CpfExecutionType;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.execution.CpfSharedApi;
import cpf.pfw.common.execution.CpfStandardExecutionId;
import cpf.pfw.common.exception.CpfErrorResponse;
import cpf.pfw.common.exception.CpfFrameworkErrorCode;
import cpf.pfw.common.exception.CpfFrameworkException;
import cpf.pfw.common.exception.CpfResolvedResponse;
import cpf.pfw.common.exception.CpfResponseCodeResolver;
import cpf.pfw.common.exception.DefaultCpfResponseCodeResolver;
import cpf.pfw.common.logging.CpfTransaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * CPF 표준 거래 헤더와 온라인 실행 메타 정보를 검증합니다.
 */
@Component
public class TransactionHeaderValidationInterceptor implements HandlerInterceptor {

    private static final Pattern LEGACY_TRANSACTION_ID_PATTERN =
            Pattern.compile("^[A-Z]{3}[0-9]{2}[A-Z0-9]{3}[0-9]{4}$");

    private final ObjectMapper objectMapper;
    private final CpfResponseCodeResolver responseCodeResolver;
    private final CpfInboundHeaderValidator inboundHeaderValidator;
    private final CpfInternalServiceIdentityVerifier internalServiceIdentityVerifier;

    @Autowired
    public TransactionHeaderValidationInterceptor(
            ObjectMapper objectMapper,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider,
            Environment environment,
            ObjectProvider<CpfInternalServiceIdentityVerifier> identityVerifierProvider) {
        this(objectMapper, responseCodeResolverProvider, environment, identityVerifierProvider.getIfAvailable());
    }

    /** 단위 테스트와 최소 구성에서 기본 내부 신원 정책을 사용하는 호환 생성자입니다. */
    public TransactionHeaderValidationInterceptor(
            ObjectMapper objectMapper,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider,
            Environment environment) {
        this(objectMapper, responseCodeResolverProvider, environment,
                (CpfInternalServiceIdentityVerifier) null);
    }

    private TransactionHeaderValidationInterceptor(
            ObjectMapper objectMapper,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider,
            Environment environment,
            CpfInternalServiceIdentityVerifier identityVerifier) {
        this.objectMapper = objectMapper;
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultCpfResponseCodeResolver::new);
        int transactionIdSequenceDigits = environment.getProperty(
                "cpf.framework.transaction-id.sequence-digits",
                Integer.class,
                7);
        this.inboundHeaderValidator = new CpfInboundHeaderValidator(transactionIdSequenceDigits);
        this.internalServiceIdentityVerifier = identityVerifier != null
                ? identityVerifier
                : defaultIdentityVerifier(environment);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        try {
            // API 유형과 무관하게 민감정보를 확장 헤더로 우회 전파하는 요청은 차단합니다.
            validateExtensionHeaders(request);

            OnlineExecutionMetadata transaction = resolveTransactionAnnotation(handlerMethod);
            if (transaction == null) {
                // 운영 조회, health, callback처럼 업무 거래로 선언하지 않은 API에는 거래 헤더를 강제하지 않습니다.
                return true;
            }
            validateRequiredHeaders(request);
            validateTransactionMetadata(transaction);
            validateStandardExecutionHeader(request, transaction);
            validateSharedApiIngress(request, transaction);
        } catch (CpfFrameworkException ex) {
            writeFrameworkError(response, ex);
            return false;
        }
        return true;
    }

    /**
     * PFW 공통 거래 처리에 필요한 필수 업무 헤더가 누락되었는지 확인합니다.
     */
    private void validateRequiredHeaders(HttpServletRequest request) {
        List<String> missingHeaders = new ArrayList<>(inboundHeaderValidator.missingRequiredHeaders(request));

        if (!missingHeaders.isEmpty()) {
            String headerNames = String.join(", ", missingHeaders);
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    "필수 거래 헤더가 누락되었습니다. " + headerNames,
                    Map.of("0", headerNames, "1", request.getRequestURI()));
        }

        String transactionId = request.getHeader(CpfHeaderNames.TRANSACTION_ID);
        if (!inboundHeaderValidator.isValidTransactionId(transactionId)) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    "트랜잭션 글로벌 ID 형식이 올바르지 않습니다. "
                            + CpfHeaderNames.TRANSACTION_ID + "=" + transactionId,
                    Map.of("0", CpfHeaderNames.TRANSACTION_ID, "1", request.getRequestURI()));
        }

    }

    /**
     * CPF 확장 헤더 이름과 민감정보 우회 전파 금지 규칙을 모든 Controller 요청에 적용합니다.
     */
    private void validateExtensionHeaders(HttpServletRequest request) {
        List<String> invalidExtensionHeaders = inboundHeaderValidator.invalidExtensionHeaders(request);
        if (!invalidExtensionHeaders.isEmpty()) {
            String headerNames = String.join(", ", invalidExtensionHeaders);
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    "CPF 확장 헤더는 X-Cpf-Ext-* naming rule을 따르되 인증값, token, API key, secret류를 우회 저장하거나 전파할 수 없습니다. "
                            + headerNames,
                    Map.of("0", headerNames, "1", request.getRequestURI()));
        }
    }

    /**
     * Controller 또는 메서드에 선언된 거래 메타 정보가 표준 형식을 만족하는지 확인합니다.
     */
    private void validateTransactionMetadata(OnlineExecutionMetadata transaction) {
        boolean valid = transaction.standard()
                ? CpfStandardExecutionId.isValid(transaction.id())
                : LEGACY_TRANSACTION_ID_PATTERN.matcher(transaction.id()).matches();
        if (!valid) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "온라인 실행 ID 형식이 올바르지 않습니다. "
                            + transaction.id(),
                    Map.of("0", "executionId", "1", transaction.id()));
        }
        if (transaction.standard()
                && CpfStandardExecutionId.parse(transaction.id()).type() != transaction.executionType()) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "표준 실행 ID 유형과 선언된 실행 유형이 일치하지 않습니다. " + transaction.id(),
                    Map.of("0", "executionType", "1", transaction.id()));
        }
        if (transaction.name() == null || transaction.name().isBlank()) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "온라인 실행 이름은 필수입니다.",
                    Map.of("0", "executionName"));
        }
    }

    private OnlineExecutionMetadata resolveTransactionAnnotation(HandlerMethod handlerMethod) {
        CpfOnlineTransaction standard = handlerMethod.getMethodAnnotation(CpfOnlineTransaction.class);
        if (standard == null) {
            standard = handlerMethod.getBeanType().getAnnotation(CpfOnlineTransaction.class);
        }
        if (standard != null) {
            return new OnlineExecutionMetadata(
                    standard.id(), standard.name(), true, CpfExecutionType.ONLINE, Set.of());
        }
        CpfSharedApi shared = handlerMethod.getMethodAnnotation(CpfSharedApi.class);
        if (shared == null) {
            shared = handlerMethod.getBeanType().getAnnotation(CpfSharedApi.class);
        }
        if (shared != null) {
            Set<String> allowedCallers = Arrays.stream(shared.allowedCallers())
                    .filter(this::hasText)
                    .map(value -> value.trim().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toUnmodifiableSet());
            return new OnlineExecutionMetadata(
                    shared.id(), shared.name(), true, CpfExecutionType.SHARED, allowedCallers);
        }
        CpfTransaction methodAnnotation = handlerMethod.getMethodAnnotation(CpfTransaction.class);
        if (methodAnnotation != null) {
            return new OnlineExecutionMetadata(
                    methodAnnotation.id(), methodAnnotation.name(), false, null, Set.of());
        }
        CpfTransaction typeAnnotation = handlerMethod.getBeanType().getAnnotation(CpfTransaction.class);
        return typeAnnotation == null
                ? null
                : new OnlineExecutionMetadata(typeAnnotation.id(), typeAnnotation.name(), false, null, Set.of());
    }

    /** 내부 서비스 호출은 호출 대상과 표준 실행 ID가 정확히 일치해야 합니다. */
    private void validateStandardExecutionHeader(HttpServletRequest request, OnlineExecutionMetadata transaction) {
        if (!transaction.standard()) {
            return;
        }
        String suppliedId = request.getHeader(CpfHeaderNames.STANDARD_EXECUTION_ID);
        boolean internalCall = hasText(request.getHeader(CpfHeaderNames.CALLER_SERVICE));
        if (internalCall && !hasText(suppliedId)) {
            throw invalidExecutionHeader(transaction.id(), "내부 호출에는 표준 실행 ID 헤더가 필수입니다.");
        }
        if (hasText(suppliedId) && !transaction.id().equals(suppliedId)) {
            throw invalidExecutionHeader(transaction.id(), "표준 실행 ID 헤더와 호출 대상이 일치하지 않습니다.");
        }
    }

    /**
     * S형 공유 API를 공개 Gateway 또는 신원이 확인되지 않은 호출자가 우회 실행하지 못하게 차단합니다.
     */
    private void validateSharedApiIngress(HttpServletRequest request, OnlineExecutionMetadata transaction) {
        if (transaction.executionType() != CpfExecutionType.SHARED) {
            return;
        }

        String suppliedId = request.getHeader(CpfHeaderNames.STANDARD_EXECUTION_ID);
        String callerService = normalize(request.getHeader(CpfHeaderNames.CALLER_SERVICE));
        String callerInstance = normalize(request.getHeader(CpfHeaderNames.CALLER_INSTANCE_ID));
        String ingressType = normalize(request.getHeader(CpfHeaderNames.INGRESS_TYPE));

        if (!hasText(suppliedId)) {
            throw accessDenied("S형 공유 API에는 표준 실행 ID 헤더가 필수입니다.");
        }
        if (!hasText(callerService) || !hasText(callerInstance)) {
            throw accessDenied("S형 공유 API에는 호출 서비스와 호출 인스턴스 신원이 모두 필요합니다.");
        }
        if (hasGatewayIdentity(request)
                || "PFW_GATEWAY".equalsIgnoreCase(ingressType)
                || "EXTERNAL_GATEWAY".equalsIgnoreCase(ingressType)) {
            throw accessDenied("S형 공유 API는 공개 Gateway 경로로 실행할 수 없습니다.");
        }
        if (!transaction.allowedCallers().isEmpty()
                && !transaction.allowedCallers().contains(callerService.toUpperCase(Locale.ROOT))) {
            throw accessDenied("허용되지 않은 CPF 서비스가 공유 API를 호출했습니다.");
        }
        if (!internalServiceIdentityVerifier.isTrusted(request, callerService, callerInstance)) {
            throw accessDenied("CPF 내부 서비스 신원을 검증할 수 없습니다.");
        }
    }

    private boolean hasGatewayIdentity(HttpServletRequest request) {
        return hasText(request.getHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID))
                || hasText(request.getHeader(CpfHeaderNames.GATEWAY_ROUTE_ID))
                || hasText(request.getHeader(CpfHeaderNames.GATEWAY_ROUTE_VERSION));
    }

    private CpfFrameworkException accessDenied(String detail) {
        return new CpfFrameworkException(
                CpfFrameworkErrorCode.INTERNAL_SERVICE_ACCESS_DENIED,
                detail,
                Map.of("0", detail));
    }

    /** 운영 프로필은 fail-closed하고 개발 프로필의 loopback 또는 명시한 peer만 허용합니다. */
    private CpfInternalServiceIdentityVerifier defaultIdentityVerifier(Environment environment) {
        Set<String> activeProfiles = Arrays.stream(environment.getActiveProfiles())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        boolean productionProfile = activeProfiles.contains("prod") || activeProfiles.contains("stg");
        Set<String> trustedPeers = Arrays.stream(environment.getProperty(
                        "cpf.framework.internal-api.trusted-peer-addresses", "").split(","))
                .map(String::trim)
                .filter(this::hasText)
                .collect(Collectors.toUnmodifiableSet());

        return (request, callerServiceId, callerInstanceId) -> {
            if (hasClientCertificate(request) || trustedPeers.contains(request.getRemoteAddr())) {
                return true;
            }
            return !productionProfile && isLoopback(request.getRemoteAddr());
        };
    }

    private boolean hasClientCertificate(HttpServletRequest request) {
        Object certificate = request.getAttribute("jakarta.servlet.request.X509Certificate");
        return certificate instanceof X509Certificate[] certificates && certificates.length > 0;
    }

    private boolean isLoopback(String address) {
        return "127.0.0.1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address)
                || "::1".equals(address);
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private CpfFrameworkException invalidExecutionHeader(String expectedId, String detail) {
        return new CpfFrameworkException(
                CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                detail + " expected=" + expectedId,
                Map.of("0", CpfHeaderNames.STANDARD_EXECUTION_ID, "1", expectedId));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record OnlineExecutionMetadata(
            String id,
            String name,
            boolean standard,
            CpfExecutionType executionType,
            Set<String> allowedCallers) {
    }

    /**
     * 인터셉터 단계에서 차단한 프레임워크 오류를 JSON 표준 응답으로 직접 기록합니다.
     */
    private void writeFrameworkError(HttpServletResponse response, CpfFrameworkException ex) throws IOException {
        CpfResolvedResponse resolvedResponse = responseCodeResolver.resolve(
                ex.getErrorCode(),
                java.util.Locale.KOREAN,
                ex.getMessageArguments(),
                ex.getDetail());
        String externalMessage = resolvedResponse.externalMessage();
        CpfErrorResponse errorResponse = CpfErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        response.setStatus(resolvedResponse.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Error-Code", resolvedResponse.errorCode());
        response.setHeader("X-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Cpf-Response-Code", resolvedResponse.responseCode());
        response.setHeader("X-Cpf-Response-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Cpf-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Error-Type", ex.getClass().getSimpleName());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
