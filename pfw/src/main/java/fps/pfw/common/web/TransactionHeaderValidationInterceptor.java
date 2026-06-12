package fps.pfw.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import fps.pfw.common.exception.FpsErrorDefinition;
import fps.pfw.common.exception.FpsErrorResponse;
import fps.pfw.common.exception.FpsFrameworkErrorCode;
import fps.pfw.common.exception.FpsFrameworkException;
import fps.pfw.common.exception.FpsMessageFormatter;
import fps.pfw.common.logging.FpsTransaction;
import fps.pfw.common.logging.TransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TransactionHeaderValidationInterceptor implements HandlerInterceptor {

    private static final Pattern BUSINESS_TRANSACTION_ID_PATTERN = Pattern.compile("^[A-Z]{3}[0-9]{2}[A-Z0-9]{3}[0-9]{4}$");
    private final ObjectMapper objectMapper;

    public TransactionHeaderValidationInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        try {
            validateRequiredHeaders(request);
            validateTransactionMetadata(handlerMethod);
        } catch (FpsFrameworkException ex) {
            writeFrameworkError(response, ex);
            return false;
        }
        return true;
    }

    private void validateRequiredHeaders(HttpServletRequest request) {
        List<String> missingHeaders = new ArrayList<>();
        require(request, TransactionContext.HEADER_REQUEST_TYPE, missingHeaders);
        require(request, TransactionContext.HEADER_ORIGINAL_CHANNEL_CODE, missingHeaders);
        require(request, TransactionContext.HEADER_CHANNEL_CODE, missingHeaders);

        if (!missingHeaders.isEmpty()) {
            String headerNames = String.join(", ", missingHeaders);
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    "필수 거래 헤더가 누락되었습니다: " + headerNames,
                    Map.of("headerNames", headerNames));
        }
    }

    private void validateTransactionMetadata(HandlerMethod handlerMethod) {
        FpsTransaction transaction = resolveTransactionAnnotation(handlerMethod);
        if (transaction == null) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction 거래 메타데이터가 누락되었습니다.",
                    Map.of("metadataName", "@FpsTransaction"));
        }
        if (!BUSINESS_TRANSACTION_ID_PATTERN.matcher(transaction.id()).matches()) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction.id는 {주제영역3}{거래유형2}{중간도메인3}{일련번호4} 형식이어야 합니다: " + transaction.id(),
                    Map.of("metadataName", "@FpsTransaction.id", "metadataValue", transaction.id()));
        }
        if (transaction.name() == null || transaction.name().isBlank()) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction.name은 필수입니다.",
                    Map.of("metadataName", "@FpsTransaction.name"));
        }
    }

    private FpsTransaction resolveTransactionAnnotation(HandlerMethod handlerMethod) {
        FpsTransaction methodAnnotation = handlerMethod.getMethodAnnotation(FpsTransaction.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(FpsTransaction.class);
    }

    private void require(HttpServletRequest request, String headerName, List<String> missingHeaders) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            missingHeaders.add(headerName);
        }
    }

    private void writeFrameworkError(HttpServletResponse response, FpsFrameworkException ex) throws IOException {
        FpsErrorDefinition errorCode = ex.getErrorCode();
        String externalTemplate = errorCode.getDefaultExternalMessage();
        String externalMessage = FpsMessageFormatter.format(externalTemplate, ex.getMessageArguments());
        FpsErrorResponse errorResponse = FpsErrorResponse.of(
                errorCode,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Error-Code", errorCode.getStatusCode());
        response.setHeader("X-Message-Code", errorCode.getMessageCode());
        response.setHeader("X-Error-Type", ex.getClass().getSimpleName());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
