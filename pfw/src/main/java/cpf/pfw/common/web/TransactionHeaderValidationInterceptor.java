package cpf.pfw.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.exception.DefaultFpsResponseCodeResolver;
import cpf.pfw.common.exception.FpsErrorResponse;
import cpf.pfw.common.exception.FpsFrameworkErrorCode;
import cpf.pfw.common.exception.FpsFrameworkException;
import cpf.pfw.common.exception.FpsResolvedResponse;
import cpf.pfw.common.exception.FpsResponseCodeResolver;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.pfw.common.logging.TransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
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
    private final FpsResponseCodeResolver responseCodeResolver;

    public TransactionHeaderValidationInterceptor(
            ObjectMapper objectMapper,
            ObjectProvider<FpsResponseCodeResolver> responseCodeResolverProvider) {
        this.objectMapper = objectMapper;
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultFpsResponseCodeResolver::new);
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
                    "?꾩닔 嫄곕옒 ?ㅻ뜑媛 ?꾨씫?섏뿀?듬땲?? " + headerNames,
                    Map.of("0", headerNames, "1", request.getRequestURI()));
        }
    }

    private void validateTransactionMetadata(HandlerMethod handlerMethod) {
        FpsTransaction transaction = resolveTransactionAnnotation(handlerMethod);
        if (transaction == null) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction 嫄곕옒 硫뷀??곗씠?곌? ?꾨씫?섏뿀?듬땲??",
                    Map.of("0", "@FpsTransaction"));
        }
        if (!BUSINESS_TRANSACTION_ID_PATTERN.matcher(transaction.id()).matches()) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction.id??{二쇱젣?곸뿭3}{嫄곕옒?좏삎2}{以묎컙?꾨찓??}{?쇰젴踰덊샇4} ?뺤떇?댁뼱???⑸땲?? " + transaction.id(),
                    Map.of("0", "@FpsTransaction.id", "1", transaction.id()));
        }
        if (transaction.name() == null || transaction.name().isBlank()) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    "@FpsTransaction.name? ?꾩닔?낅땲??",
                    Map.of("0", "@FpsTransaction.name"));
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
        FpsResolvedResponse resolvedResponse = responseCodeResolver.resolve(
                ex.getErrorCode(),
                java.util.Locale.KOREAN,
                ex.getMessageArguments(),
                ex.getDetail());
        String externalMessage = resolvedResponse.externalMessage();
        FpsErrorResponse errorResponse = FpsErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        response.setStatus(resolvedResponse.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Error-Code", resolvedResponse.errorCode());
        response.setHeader("X-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Fps-Response-Code", resolvedResponse.responseCode());
        response.setHeader("X-Fps-Response-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Fps-Message-Code", resolvedResponse.messageCode());
        response.setHeader("X-Error-Type", ex.getClass().getSimpleName());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

