package com.cpf.foundation.version;

import com.cpf.core.api.version.CpfPlatformVersion;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/** Build/classpath version metadata를 Core value로 변환하는 Foundation Runtime Loader입니다. */
public final class CpfPlatformVersionLoader {
    public static final String RESOURCE = "META-INF/cpf-platform.properties";
    private final ClassLoader classLoader;

    public CpfPlatformVersionLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    public CpfPlatformVersion load() {
        Properties properties = new Properties();
        try (InputStream input = classLoader.getResourceAsStream(RESOURCE)) {
            if (input != null) properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("CPF version metadata를 읽을 수 없습니다.", ex);
        }
        String platform = value(properties, "platformVersion", packageVersion());
        String compatible = value(properties, "compatibleRange", "UNKNOWN");
        String componentVersion = value(properties, "componentVersion", platform);
        String component = value(properties, "component", "cpf");
        return new CpfPlatformVersion(platform, compatible, componentVersion, component);
    }

    private String packageVersion() {
        Package pkg = CpfPlatformVersionLoader.class.getPackage();
        String version = pkg == null ? null : pkg.getImplementationVersion();
        return version == null || version.isBlank() ? "UNKNOWN" : version.trim();
    }

    private static String value(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
