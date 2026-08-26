package com.cpf.web.runtime;

import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Build에서 생성한 업무 Online Operation Manifest와 실제 Spring Handler Mapping을 대조합니다.
 * Manifest는 Source 사실을 조기에 검증하는 Build artifact일 뿐 운영 DB를 수정하지 않습니다.
 */
final class CpfBusinessOperationManifestVerifier {
    static final String RESOURCE_PATTERN = "classpath*:META-INF/cpf/business-operation-manifest.json";

    private final ObjectMapper objectMapper;
    private final PathMatchingResourcePatternResolver resources;

    CpfBusinessOperationManifestVerifier() {
        this(new ObjectMapper(), new PathMatchingResourcePatternResolver());
    }

    CpfBusinessOperationManifestVerifier(ObjectMapper objectMapper, PathMatchingResourcePatternResolver resources) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    void verify(List<CpfOperationCatalogRegistry.Operation> runtimeOperations, boolean required) {
        List<CpfOperationCatalogRegistry.Operation> runtime = runtimeOperations == null
                ? List.of()
                : runtimeOperations.stream().sorted(Comparator.comparing(value -> value.operationId())).toList();
        Set<String> runtimeIds = runtime.stream().map(value -> value.operationId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<Manifest> manifests = loadManifests();
        List<Manifest> matching = manifests.stream()
                .filter(manifest -> manifest.operationIds().equals(runtimeIds))
                .toList();

        if (matching.isEmpty()) {
            if (required) {
                throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_MISSING:runtime=" + runtimeIds
                        + ":available=" + manifests.stream().map(value -> value.projectPath()).toList());
            }
            return;
        }
        if (matching.size() != 1) {
            throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_AMBIGUOUS:runtime=" + runtimeIds
                    + ":matches=" + matching.stream().map(value -> value.projectPath()).toList());
        }

        Manifest manifest = matching.getFirst();
        if (manifest.schemaVersion() != 1) {
            throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_SCHEMA_UNSUPPORTED:"
                    + manifest.projectPath() + ":" + manifest.schemaVersion());
        }
        Map<String, ManifestOperation> byId = manifest.byOperationId();
        for (CpfOperationCatalogRegistry.Operation operation : runtime) {
            ManifestOperation expected = byId.get(operation.operationId());
            if (expected == null) {
                throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_OPERATION_MISSING:" + operation.operationId());
            }
            compare("name", operation.operationId(), expected.name(), operation.name());
            compare("description", operation.operationId(), expected.description(), operation.description());
            compare("openApiOperationId", operation.operationId(), expected.openApiOperationId(), operation.operationId());
            compare("httpMethod", operation.operationId(), expected.httpMethod(), operation.httpMethod());
            compare("apiPath", operation.operationId(), expected.apiPath(), operation.apiPath());
            compare("controllerClass", operation.operationId(), expected.controllerClass(), operation.controllerClass());
            compare("handlerMethod", operation.operationId(), expected.handlerMethod(), operation.handlerMethod());
            compare("sourceFingerprint", operation.operationId(), expected.sourceFingerprint(), operation.sourceFingerprint());
        }
    }

    private List<Manifest> loadManifests() {
        try {
            Resource[] found = resources.getResources(RESOURCE_PATTERN);
            ArrayList<Manifest> manifests = new ArrayList<>();
            for (Resource resource : found) {
                try (var in = resource.getInputStream()) {
                    Manifest manifest = objectMapper.readValue(in, Manifest.class);
                    if (manifest != null) manifests.add(manifest.normalized());
                }
            }
            manifests.sort(Comparator.comparing(value -> value.projectPath()));
            return List.copyOf(manifests);
        } catch (IOException e) {
            throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_READ_FAILED", e);
        }
    }

    private static void compare(String field, String operationId, String expected, String actual) {
        if (!Objects.equals(normalize(expected), normalize(actual))) {
            throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_MISMATCH:"
                    + operationId + ":" + field + ":expected=" + expected + ":actual=" + actual);
        }
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    record Manifest(int schemaVersion, String projectPath, List<ManifestOperation> operations) {
        Manifest normalized() {
            String path = projectPath == null || projectPath.isBlank() ? "UNKNOWN" : projectPath.trim();
            List<ManifestOperation> values = operations == null ? List.of() : operations.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(value -> value.operationId()))
                    .toList();
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            for (ManifestOperation operation : values) {
                if (operation.operationId() == null || operation.operationId().isBlank()) {
                    throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_EMPTY_OPERATION_ID:" + path);
                }
                if (!ids.add(operation.operationId())) {
                    throw new IllegalStateException("CPF_BUSINESS_OPERATION_MANIFEST_DUPLICATE_OPERATION_ID:"
                            + path + ":" + operation.operationId());
                }
            }
            return new Manifest(schemaVersion, path, values);
        }

        Set<String> operationIds() {
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            for (ManifestOperation operation : operations) ids.add(operation.operationId());
            return Set.copyOf(ids);
        }

        Map<String, ManifestOperation> byOperationId() {
            LinkedHashMap<String, ManifestOperation> values = new LinkedHashMap<>();
            for (ManifestOperation operation : operations) values.put(operation.operationId(), operation);
            return Map.copyOf(values);
        }
    }

    record ManifestOperation(
            String operationId,
            String name,
            String description,
            String openApiOperationId,
            String httpMethod,
            String apiPath,
            String controllerClass,
            String handlerMethod,
            String sourceFingerprint,
            String sourcePath) {}
}
