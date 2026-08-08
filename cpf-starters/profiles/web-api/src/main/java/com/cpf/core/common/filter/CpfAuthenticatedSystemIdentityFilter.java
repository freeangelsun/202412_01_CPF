package com.cpf.core.common.filter;

import com.cpf.core.common.transaction.CpfInboundTransactionIdPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 서버가 인증한 caller identity를 CPF SystemCode로 변환하는 최초 ingress adapter입니다.
 *
 * <p>클라이언트가 보낸 Channel/System 헤더는 신뢰 근거로 사용하지 않습니다. Servlet container가
 * 검증한 {@link Principal} 또는 mTLS 인증서만 입력으로 사용하고, principal→SystemCode 매핑은
 * 서버 설정 {@code cpf.security.authenticated-system-map.<principal>}에서 가져옵니다. 인증서 CN은
 * {@code CPF-SYS-ADM} 또는 정확한 3자리 SystemCode 형식일 때만 직접 허용합니다.</p>
 *
 * <p>이 필터는 {@link TransactionContextFilter}보다 먼저 실행되어 transactionId trust policy가
 * 평가되기 전에 server-side attribute를 생성합니다. Attribute는 HTTP 요청 헤더로 주입할 수 없어
 * spoofing boundary를 유지합니다.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public final class CpfAuthenticatedSystemIdentityFilter extends OncePerRequestFilter {
    private static final Pattern CERT_CN = Pattern.compile("(?:^|,)\\s*CN=([^,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SYSTEM_CODE = Pattern.compile("[A-Z0-9]{3}");
    private final Environment environment;

    public CpfAuthenticatedSystemIdentityFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String systemCode = authenticatedSystemCode(request);
        if (systemCode != null) {
            request.setAttribute(CpfInboundTransactionIdPolicy.AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE, systemCode);
            request.setAttribute(CpfInboundTransactionIdPolicy.TRUSTED_CONTEXT_ATTRIBUTE, Boolean.TRUE);
        }
        chain.doFilter(request, response);
    }

    private String authenticatedSystemCode(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            String mapped = environment.getProperty(
                    "cpf.security.authenticated-system-map." + principal.getName().trim());
            String normalized = normalize(mapped);
            if (normalized != null) return normalized;
        }
        Object certs = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certs instanceof X509Certificate[] array && array.length > 0 && array[0] != null) {
            Matcher matcher = CERT_CN.matcher(array[0].getSubjectX500Principal().getName());
            if (matcher.find()) {
                String cn = matcher.group(1).trim().toUpperCase(Locale.ROOT);
                if (cn.startsWith("CPF-SYS-")) cn = cn.substring("CPF-SYS-".length());
                return normalize(cn);
            }
        }
        return null;
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return SYSTEM_CODE.matcher(normalized).matches() ? normalized : null;
    }
}
