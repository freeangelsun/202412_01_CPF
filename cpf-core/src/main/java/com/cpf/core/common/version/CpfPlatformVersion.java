package com.cpf.core.common.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 빌드가 생성한 단일 버전 metadata를 조회합니다.
 *
 * <p>업무 모듈은 버전을 별도로 하드코딩하지 않고 OpenAPI, ADM 정보 화면과 진단 API에서 이 값을 사용합니다.</p>
 *
 * @since 1.0.0
 */
public final class CpfPlatformVersion {
    private static final String RESOURCE = "META-INF/cpf-platform.properties";
    private static final Properties PROPERTIES = load();

    private CpfPlatformVersion() {
    }

    /**
     * 현재 CPF platform 버전을 반환합니다.
     *
     * @return 제품 버전 또는 개발 classpath의 UNKNOWN
     */
    public static String current() {
        return PROPERTIES.getProperty("platformVersion", packageVersion());
    }

    /**
     * 현재 구성요소의 호환 범위를 반환합니다.
     *
     * @return CPF 호환 범위
     */
    public static String compatibleRange() {
        return PROPERTIES.getProperty("compatibleRange", "UNKNOWN");
    }

    /**
     * 현재 실행 구성요소 버전을 반환합니다.
     *
     * @return 구성요소 버전
     */
    public static String componentVersion() {
        return PROPERTIES.getProperty("componentVersion", current());
    }

    /**
     * 현재 구성요소 이름을 반환합니다.
     *
     * @return Gradle component 이름
     */
    public static String component() {
        return PROPERTIES.getProperty("component", "cpf");
    }

    private static Properties load() {
        Properties properties = new Properties();
        ClassLoader loader = CpfPlatformVersion.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(RESOURCE)) {
            if (input != null) {
                properties.load(input);
            }
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("CPF 버전 metadata를 읽을 수 없습니다.", ex);
        }
    }

    private static String packageVersion() {
        String version = CpfPlatformVersion.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "UNKNOWN" : version;
    }
}
