package com.cpf.local.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Generator로 생성된 업무 Domain을 같은 JVM/한 Port에 자동 조립합니다.
 *
 * <p>Generated Domain JAR의 {@code META-INF/cpf/generated-domain.properties}를 읽어 scan package를
 * 자동 발견합니다. 수동 base-package는 특수 고객 확장용 escape hatch로만 유지합니다.</p>
 */
public final class CpfLocalDomainModuleRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final String DESCRIPTOR = "META-INF/cpf/generated-domain.properties";
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) { this.environment = environment; }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (!environment.getProperty("cpf.local.modules.domains.enabled", Boolean.class, true)) return;

        LinkedHashSet<String> packages = new LinkedHashSet<>();
        if (environment.getProperty("cpf.local.modules.domains.auto-discover", Boolean.class, true)) {
            packages.addAll(discoverGeneratedPackages());
        }
        String configured = environment.getProperty("cpf.local.modules.domains.base-packages", "");
        Arrays.stream(configured.split(","))
                .map(value -> value.trim())
                .filter(s -> !s.isBlank())
                .forEach(packages::add);

        if (packages.isEmpty()) return; // Generated Domain을 아직 만들지 않은 신규 프로젝트도 정상 기동합니다.
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        // 각 Domain의 독립 Boot Application 자체를 다시 기동하지 않고 Component만 Local Runtime에 조립합니다.
        scanner.addExcludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
        scanner.scan(packages.toArray(String[]::new));
    }

    private List<String> discoverGeneratedPackages() {
        List<String> packages = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = loader.getResources(DESCRIPTOR);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties properties = new Properties();
                try (InputStream input = url.openStream()) { properties.load(input); }
                String scanPackage = properties.getProperty("scanPackage", "").trim();
                if (!scanPackage.isBlank()) packages.add(scanPackage);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Generated Domain descriptor를 읽지 못했습니다.", ex);
        }
        return packages.stream().distinct().toList();
    }
}
