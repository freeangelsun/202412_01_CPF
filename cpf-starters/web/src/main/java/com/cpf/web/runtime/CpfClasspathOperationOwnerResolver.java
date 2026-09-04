package com.cpf.web.runtime;

import com.cpf.web.context.CpfOperationOwnerResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.method.HandlerMethod;

/**
 * Resolves 1-WAS business-operation ownership from explicit component descriptors.
 *
 * <p>This is deliberately not an operation-ID, URL, package-name, module-name, or DB-prefix inference.
 * Each matching package is declared by the owning generated-domain descriptor or Product component descriptor;
 * the package merely selects among those explicit declarations.</p>
 */
public final class CpfClasspathOperationOwnerResolver implements CpfOperationOwnerResolver {
    static final String GENERATED_DESCRIPTOR = "classpath*:META-INF/cpf/generated-domain.properties";
    static final String PRODUCT_DESCRIPTOR = "classpath*:META-INF/cpf/runtime-component.properties";
    private final List<CpfOperationOwner> owners;

    public CpfClasspathOperationOwnerResolver() {
        this(loadOwners(new PathMatchingResourcePatternResolver()));
    }

    CpfClasspathOperationOwnerResolver(List<CpfOperationOwner> owners) {
        Map<String, CpfOperationOwner> unique = new LinkedHashMap<>();
        for (CpfOperationOwner owner : owners == null ? List.<CpfOperationOwner>of() : owners) {
            if (owner == null) continue;
            CpfOperationOwner normalized = normalize(owner);
            String key = normalized.systemCode() + "|" + normalized.domainCode() + "|" + normalized.scanPackage();
            unique.putIfAbsent(key, normalized);
        }
        this.owners = unique.values().stream()
                .sorted(Comparator.comparingInt((CpfOperationOwner value) -> value.scanPackage().length()).reversed())
                .toList();
    }

    @Override
    public CpfOperationOwner resolve(HandlerMethod handlerMethod, String operationId) {
        if (handlerMethod == null) return null;
        String packageName = handlerMethod.getBeanType().getPackageName();
        CpfOperationOwner found = null;
        for (CpfOperationOwner owner : owners) {
            if (!matches(packageName, owner.scanPackage())) continue;
            if (found == null) {
                found = owner;
                continue;
            }
            // Descriptor 목록은 가장 긴 scanPackage 우선으로 정렬되어 있다. 상위 Component
            // descriptor는 하위 explicit Owner를 덮지 않는다. 같은 길이만 실제 모호성이다.
            if (found.scanPackage().length() == owner.scanPackage().length() && !sameOwner(found, owner)) {
                throw new IllegalStateException("CPF_OPERATION_OWNER_AMBIGUOUS:" + operationId + ":" + packageName);
            }
        }
        return found;
    }

    private static List<CpfOperationOwner> loadOwners(PathMatchingResourcePatternResolver resources) {
        ArrayList<CpfOperationOwner> result = new ArrayList<>();
        try {
            load(resources.getResources(GENERATED_DESCRIPTOR), result);
            load(resources.getResources(PRODUCT_DESCRIPTOR), result);
            return List.copyOf(result);
        } catch (IOException e) {
            throw new IllegalStateException("CPF_OPERATION_OWNER_DESCRIPTOR_READ_FAILED", e);
        }
    }

    private static void load(Resource[] resources, List<CpfOperationOwner> result) throws IOException {
        for (Resource resource : resources == null ? new Resource[0] : resources) {
            Properties values = new Properties();
            try (var input = resource.getInputStream()) { values.load(input); }
            String system = required(values.getProperty("systemCode"), "systemCode", resource);
            String domain = optional(values.getProperty("domain"));
            if (domain == null) domain = optional(values.getProperty("domainCode"));
            if (domain == null) domain = system;
            String scanPackage = required(values.getProperty("scanPackage"), "scanPackage", resource);
            result.add(new CpfOperationOwner(system, domain, optional(values.getProperty("application")), scanPackage));
        }
    }

    private static CpfOperationOwner normalize(CpfOperationOwner owner) {
        String system = required(owner.systemCode(), "systemCode", null).toUpperCase(java.util.Locale.ROOT);
        if (!system.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new IllegalStateException("Invalid CPF operation owner SystemCode: " + system);
        }
        return new CpfOperationOwner(system,
                optional(owner.domainCode()) == null ? system : owner.domainCode().trim(),
                optional(owner.application()),
                required(owner.scanPackage(), "scanPackage", null));
    }

    private static boolean matches(String packageName, String scanPackage) {
        return Objects.equals(packageName, scanPackage) || packageName.startsWith(scanPackage + ".");
    }

    private static boolean sameOwner(CpfOperationOwner first, CpfOperationOwner second) {
        return first.systemCode().equals(second.systemCode())
                && first.domainCode().equals(second.domainCode())
                && Objects.equals(first.application(), second.application());
    }

    private static String required(String value, String field, Resource resource) {
        String normalized = optional(value);
        if (normalized != null) return normalized;
        String suffix = resource == null ? "" : ":" + resource.getDescription();
        throw new IllegalStateException("CPF_OPERATION_OWNER_DESCRIPTOR_" + field.toUpperCase(java.util.Locale.ROOT)
                + "_REQUIRED" + suffix);
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
