package com.cpf.core.common.http;

import com.cpf.core.common.header.CpfHeaderNames;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.Locale;

/**
 * Outbound 호출의 대상 Service ID를 고정 Domain/Port 목록 없이 해석합니다.
 *
 * <p>CPF Client는 등록 Metadata의 serviceId를 {@link CpfHeaderNames#TARGET_SERVICE}로
 * 명시합니다. 범용 RestClient처럼 대상 Metadata를 모르는 호출은 URI host만 기록하며,
 * URL path나 개발용 port로 업무 Domain을 추측하지 않습니다.</p>
 */
final class CpfTargetServiceResolver {
    private CpfTargetServiceResolver() {
    }

    static String resolve(HttpHeaders headers, URI uri) {
        String explicitServiceId = headers == null ? null : headers.getFirst(CpfHeaderNames.TARGET_SERVICE);
        if (hasText(explicitServiceId)) {
            return explicitServiceId.trim().toUpperCase(Locale.ROOT);
        }
        String host = uri == null ? null : uri.getHost();
        return hasText(host) ? host.trim().toUpperCase(Locale.ROOT) : "UNKNOWN";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
