package com.cpf.data.persistence.mybatis;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/** Resolves the selected Mapper resources packaged by a Generated Customer Domain executable. */
final class CpfDomainMapperResources {
    private static final String PROPERTY = "mybatis.mapper-locations";
    private static final String DEFAULT_PATTERN = "classpath*:db/mapper/*.xml";

    private CpfDomainMapperResources() { }

    static Resource[] resolve(Environment environment) throws IOException {
        Objects.requireNonNull(environment, "environment");
        String[] configured = environment.getProperty(PROPERTY, String[].class, new String[] {DEFAULT_PATTERN});
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, Resource> resolved = new LinkedHashMap<>();

        Arrays.stream(configured == null ? new String[0] : configured)
                .flatMap(value -> Arrays.stream(value == null ? new String[0] : value.split(",")))
                .map(value -> value.trim())
                .filter(value -> !value.isEmpty())
                .forEach(pattern -> addResources(resolver, resolved, pattern));

        if (resolved.isEmpty()) {
            throw new IllegalStateException(
                    "Generated Domain MyBatis mapper resources are required: " + PROPERTY);
        }
        return resolved.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(value -> value.getValue())
                .toArray(Resource[]::new);
    }

    private static void addResources(
            PathMatchingResourcePatternResolver resolver,
            Map<String, Resource> resolved,
            String pattern) {
        try {
            for (Resource resource : resolver.getResources(pattern)) {
                if (resource.exists() && resource.isReadable()) {
                    resolved.putIfAbsent(resource.getURL().toExternalForm(), resource);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot resolve Generated Domain MyBatis mapper pattern: " + pattern,
                    exception);
        }
    }
}
