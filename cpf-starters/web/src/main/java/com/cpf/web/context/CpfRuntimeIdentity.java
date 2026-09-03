package com.cpf.web.context;

import org.springframework.core.env.Environment;
import com.cpf.foundation.runtime.CpfRuntimeMetadata;

import java.util.Locale;

/**
 * Runtime-known HTTP identity. Business code never populates these values per request.
 *
 * <p>The runtime {@code systemCode} remains trusted runtime/registry metadata. For generated business
 * domains the exact same canonical value is also the CPF transaction {@code currentChannel}; no
 * System-to-Channel mapper or duplicate channel configuration exists.</p>
 */
public record CpfRuntimeIdentity(String systemCode, String currentChannel, String application, String instance) {
    public CpfRuntimeIdentity {
        // SystemCode 는 Architecture Role 에 따라 없을 수 있다(Harness 30.16 표).
        // ADM(Platform Control Plane) / Gateway / Channel Front / 1-WAS topology 는 SystemCode 가 없고,
        // 없다는 이유로 가상 값을 만들지 않는다. 값이 있으면 형식을 엄격히 검증한다.
        systemCode = optionalSystemCode(systemCode, "systemCode");
        currentChannel = optionalChannel(currentChannel, "currentChannel");
        application = normalize(application, 128);
        instance = normalize(instance, 160);
    }

    /** Generated-domain canonical relation: systemCode value itself is the runtime Channel identity. */
    public CpfRuntimeIdentity(String systemCode, String application, String instance) {
        this(systemCode, systemCode, application, instance);
    }

    /** 정본 ChannelCode 선언 property. SystemCode 가 없는 Component 가 lineage 를 구성하는 근거다. */
    public static final String CHANNEL_CODE_PROPERTY = "cpf.channel-code";

    public static CpfRuntimeIdentity from(Environment environment) {
        return from(CpfRuntimeMetadata.from(environment),
                environment == null ? null : environment.getProperty(CHANNEL_CODE_PROPERTY));
    }

    public static CpfRuntimeIdentity from(CpfRuntimeMetadata runtime) {
        if (runtime == null) throw new IllegalArgumentException("runtime");
        return from(runtime, null);
    }

    /**
     * Runtime Identity 를 만듭니다.
     *
     * <p>Business/Reference/Batch Runtime 은 canonical SystemCode 값이 곧 그 hop 의 ChannelCode 값이다
     * (Harness 30.2). SystemCode 가 없는 Platform/Channel Component 는 자기 정본 ChannelCode 를 선언해
     * lineage 를 구성한다 — SystemCode 를 만들어 채우지 않는다.</p>
     *
     * @param runtime Runtime Metadata
     * @param declaredChannelCode 정본 ChannelCode 선언값. 없으면 {@code null}
     * @return Runtime Identity
     */
    public static CpfRuntimeIdentity from(CpfRuntimeMetadata runtime, String declaredChannelCode) {
        if (runtime == null) throw new IllegalArgumentException("runtime");
        String channel = declaredChannelCode != null && !declaredChannelCode.isBlank()
                ? declaredChannelCode
                : runtime.systemCode();
        return new CpfRuntimeIdentity(runtime.systemCode(), channel, runtime.application(), runtime.instanceId());
    }

    private static String optionalSystemCode(String value, String name) {
        String normalized = normalize(value, 32);
        if (normalized == null) return null;
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new IllegalStateException("Invalid CPF runtime " + name + ": " + normalized);
        }
        return normalized;
    }


    private static String optionalChannel(String value, String name) {
        String normalized = normalize(value, 16);
        if (normalized == null) return null;
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,15}")) {
            throw new IllegalStateException("Invalid CPF runtime " + name + ": " + normalized);
        }
        return normalized;
    }
    private static String normalize(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > max || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException("Invalid CPF runtime identity value");
        }
        return normalized;
    }
}
