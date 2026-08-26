package com.cpf.integration.http.internal;

import com.cpf.web.context.CpfHttpHeaderNames;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.Locale;

/**
 * Outbound 호출의 대상 System Identity를 고정 Domain/Port 목록 없이 해석합니다.
 *
 * <p>CPF Client는 typed internal Domain adapter가 {@link CpfHttpHeaderNames#TARGET_SYSTEM_CODE}를
 * 구성합니다. 범용 RestClient처럼 대상 Metadata를 모르는 외부 호출은 URI host만 기록하며,
 * URL path나 개발용 port로 업무 Domain을 추측하지 않습니다.</p>
 */
final class CpfTargetSystemResolver {
    private CpfTargetSystemResolver() {
    }

    static String resolve(HttpHeaders headers, URI uri) {
        String explicitServiceId = headers == null ? null : headers.getFirst(CpfHttpHeaderNames.TARGET_SYSTEM_CODE);
        if (explicitServiceId != null) {
            String normalizedServiceId = explicitServiceId.trim();
            if (!normalizedServiceId.isEmpty()) {
                return normalizedServiceId.toUpperCase(Locale.ROOT);
            }
        }
        String host = uri == null ? null : uri.getHost();
        return host != null && !host.isBlank() ? host.trim().toUpperCase(Locale.ROOT) : "UNKNOWN";
    }

}
