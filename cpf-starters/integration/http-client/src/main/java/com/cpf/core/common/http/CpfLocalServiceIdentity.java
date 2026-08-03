package com.cpf.core.common.http;

import com.cpf.core.common.logging.ServerInstanceIdentity;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * 하위 서비스 호출 헤더에 기록할 현재 CPF 서비스 신원을 제공합니다.
 *
 * <p>외부에서 받은 호출자 헤더를 그대로 다음 서비스에 전달하지 않고 현재 서비스 ID와
 * 인스턴스 ID로 교체하여, 대상 서비스가 실제 직전 호출 주체를 판별할 수 있게 합니다.</p>
 */
public record CpfLocalServiceIdentity(String serviceId, String instanceId) {

    /** 환경설정과 서버 실행 정보를 사용해 현재 서비스 신원을 생성합니다. */
    public static CpfLocalServiceIdentity from(Environment environment) {
        String configuredServiceId = firstText(
                environment.getProperty("cpf.framework.module-id"),
                environment.getProperty("spring.application.name"),
                "UNKNOWN");
        String configuredInstanceId = firstText(
                environment.getProperty("cpf.framework.instance-id"),
                environment.getProperty("cpf.instance-id"),
                ServerInstanceIdentity.current().serverInstanceId());
        return new CpfLocalServiceIdentity(
                normalizeServiceId(configuredServiceId),
                configuredInstanceId.trim());
    }

    private static String normalizeServiceId(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("CPF-") ? normalized.substring(4) : normalized;
    }

    private static String firstText(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : fallback;
    }
}
