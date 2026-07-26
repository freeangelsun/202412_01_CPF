package com.cpf.bizadmin.auth.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * BZA API와 Frontend가 함께 사용하는 권한 메뉴 그룹 정본입니다.
 *
 * <p>정본 JSON은 {@code cpf-tools/db/metadata/bza-permission-manifest.json}이며
 * build 시 classpath의 {@code cpf/bza/bza-permission-manifest.json}으로 포함됩니다.</p>
 */
@Component
public class BzaPermissionManifest {
    static final String CLASSPATH_LOCATION = "cpf/bza/bza-permission-manifest.json";

    private final Definition definition;

    public BzaPermissionManifest(ObjectMapper objectMapper) {
        this.definition = read(objectMapper);
    }

    public Optional<String> resolveApiMenuCode(String relativeApiPath) {
        String normalizedPath = normalizePath(relativeApiPath);
        return definition.apiResourceGroups().entrySet().stream()
                .filter(entry -> normalizedPath.equals(entry.getKey())
                        || normalizedPath.startsWith(entry.getKey() + "/"))
                .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .map(this::canonicalMenuCode);
    }

    public String canonicalMenuCode(String value) {
        String normalized = normalizeCode(value);
        if (normalized.startsWith("BZA_")) {
            normalized = normalized.substring(4);
        }
        return definition.permissionAliases().getOrDefault(normalized, normalized);
    }

    public List<String> menuGroups() {
        return definition.menuGroups();
    }

    public String sourceProjection() {
        return definition.sourceProjection();
    }

    private Definition read(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource(CLASSPATH_LOCATION).getInputStream()) {
            Definition loaded = objectMapper.readValue(input, Definition.class);
            if (loaded.schemaVersion() != 1
                    || loaded.menuGroups() == null
                    || loaded.menuGroups().isEmpty()
                    || loaded.apiResourceGroups() == null
                    || loaded.apiResourceGroups().isEmpty()) {
                throw new IllegalStateException("BZA permission manifest 필수 Metadata가 없습니다.");
            }
            Map<String, String> resources = new LinkedHashMap<>();
            loaded.apiResourceGroups().forEach((key, value) ->
                    resources.put(normalizePath(key), normalizeCode(value)));
            Map<String, String> aliases = new LinkedHashMap<>();
            if (loaded.permissionAliases() != null) {
                loaded.permissionAliases().forEach((key, value) ->
                        aliases.put(normalizeCode(key), normalizeCode(value)));
            }
            return new Definition(
                    loaded.schemaVersion(),
                    loaded.owner(),
                    loaded.sourceProjection(),
                    loaded.menuGroups().stream().map(BzaPermissionManifest::normalizeCode).toList(),
                    Map.copyOf(resources),
                    Map.copyOf(aliases));
        } catch (IOException error) {
            throw new IllegalStateException("BZA permission manifest를 읽을 수 없습니다.", error);
        }
    }

    private static String normalizePath(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\\', '/').replaceAll("^/+|/+$", "").toLowerCase(Locale.ROOT);
    }

    private static String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record Definition(
            int schemaVersion,
            String owner,
            String sourceProjection,
            List<String> menuGroups,
            Map<String, String> apiResourceGroups,
            Map<String, String> permissionAliases) {
    }
}
