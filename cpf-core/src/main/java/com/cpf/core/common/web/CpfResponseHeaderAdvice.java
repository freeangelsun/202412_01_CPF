package cpf.pfw.common.web;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Adds CPF standard response metadata headers when a response body exposes them.
 *
 * <p>The response body is the source of truth. Headers are optional metadata for
 * gateways, logs, and clients that need fast access without parsing the body.</p>
 */
@ControllerAdvice
public class CpfResponseHeaderAdvice implements ResponseBodyAdvice<Object> {
    public static final String RESPONSE_CODE_HEADER = "X-Cpf-Response-Code";
    public static final String RESPONSE_MESSAGE_CODE_HEADER = "X-Cpf-Response-Message-Code";
    public static final String MESSAGE_CODE_HEADER = "X-Cpf-Message-Code";
    public static final String MESSAGE_CONTENT_HEADER = "X-Cpf-Message-Content";
    public static final String MESSAGE_ID_HEADER = "X-Cpf-Message-Id";
    public static final String TRANSACTION_ID_HEADER = "X-Cpf-Transaction-Id";
    public static final String TRACE_ID_HEADER = "X-Cpf-Trace-Id";

    private static final int MAX_HEADER_VALUE_LENGTH = 500;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null || body instanceof String) {
            return body;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(body);
        String responseCode = property(wrapper, "statusCode");
        String messageCode = property(wrapper, "messageCode");
        String message = firstText(property(wrapper, "messageContent"), property(wrapper, "message"));

        setHeader(response, RESPONSE_CODE_HEADER, responseCode);
        setHeader(response, RESPONSE_MESSAGE_CODE_HEADER, messageCode);
        setHeader(response, MESSAGE_CODE_HEADER, messageCode);
        setHeader(response, MESSAGE_CONTENT_HEADER, encodeHeaderValue(message));
        setHeader(response, MESSAGE_ID_HEADER, property(wrapper, "messageId"));
        setHeader(response, TRANSACTION_ID_HEADER, property(wrapper, "transactionId"));
        setHeader(response, TRACE_ID_HEADER, property(wrapper, "traceId"));
        return body;
    }

    private String property(BeanWrapper wrapper, String propertyName) {
        if (!wrapper.isReadableProperty(propertyName)) {
            return null;
        }
        Object value = wrapper.getPropertyValue(propertyName);
        return value == null ? null : String.valueOf(value);
    }

    private void setHeader(ServerHttpResponse response, String name, String value) {
        if (value != null && !value.isBlank()) {
            response.getHeaders().set(name, truncate(value));
        }
    }

    private String encodeHeaderValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String truncate(String value) {
        return value.length() <= MAX_HEADER_VALUE_LENGTH
                ? value
                : value.substring(0, MAX_HEADER_VALUE_LENGTH);
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}

