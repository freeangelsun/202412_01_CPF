package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Vendor별 Repository SQL 파일을 읽는 fail-closed catalog입니다.
 *
 * <p>중앙 Vendor Pack을 선택한 경우
 * {@code {resource-root}/runtime/{module}/repository/{statement}.sql}만 읽습니다.
 * 외부 pack을 선택하지 않은 호환 모드에서만
 * {@code sql/vendor/{vendor}/{module}/{statement}.sql} classpath resource를 읽습니다.
 * 모든 Vendor resource는 같은 statement key와 parameter contract를 가져야 하며
 * 업무 Service는 Vendor를 알지 못합니다.</p>
 */
public final class CpfVendorSqlCatalog {
    private static final Pattern SAFE_TOKEN = Pattern.compile("[a-z][a-z0-9_-]{1,63}");

    private final CpfDatabaseVendor vendor;
    private final String moduleCode;
    private final ClassLoader classLoader;
    private final Path externalResourceRoot;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private CpfVendorSqlCatalog(
            CpfDatabaseVendor vendor,
            String moduleCode,
            ClassLoader classLoader,
            Path externalResourceRoot) {
        this.vendor = vendor;
        this.moduleCode = requireSafeToken(moduleCode, "moduleCode");
        this.classLoader = classLoader;
        this.externalResourceRoot = externalResourceRoot;
    }

    public static CpfVendorSqlCatalog create(Environment environment, String moduleCode) {
        CpfDatabaseVendor vendor = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"));
        Optional<Path> externalRoot = CpfVendorResourceRoot.selected(environment, vendor);
        return new CpfVendorSqlCatalog(
                vendor,
                moduleCode,
                CpfVendorSqlCatalog.class.getClassLoader(),
                externalRoot.orElse(null));
    }

    static CpfVendorSqlCatalog create(
            CpfDatabaseVendor vendor,
            String moduleCode,
            ClassLoader classLoader) {
        return new CpfVendorSqlCatalog(vendor, moduleCode, classLoader, null);
    }

    public String required(String statementKey) {
        String safeKey = requireSafeToken(statementKey, "statementKey");
        return cache.computeIfAbsent(safeKey, this::read);
    }

    public String resourcePath(String statementKey) {
        String safeKey = requireSafeToken(statementKey, "statementKey");
        if (externalResourceRoot != null) {
            return externalRelativePath(safeKey).toString().replace('\\', '/');
        }
        return "sql/vendor/%s/%s/%s.sql".formatted(vendor.id(), moduleCode, safeKey);
    }

    public CpfDatabaseVendor vendor() {
        return vendor;
    }

    private String read(String statementKey) {
        if (externalResourceRoot != null) {
            return readExternal(statementKey);
        }
        return readClasspath(statementKey);
    }

    private String readExternal(String statementKey) {
        Path relativePath = externalRelativePath(statementKey);
        Path resource = CpfVendorResourceRoot.requiredFile(
                externalResourceRoot,
                relativePath,
                "Repository SQL");
        try {
            String sql = Files.readString(resource, StandardCharsets.UTF_8).trim();
            if (sql.isEmpty()) {
                throw new IllegalStateException(
                        "Vendor Repository SQL이 비어 있습니다. path=" + resource);
            }
            return sql;
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Vendor Repository SQL을 읽을 수 없습니다. path=" + resource,
                    ex);
        }
    }

    private String readClasspath(String statementKey) {
        String path = resourcePath(statementKey);
        ClassPathResource resource = new ClassPathResource(path, classLoader);
        if (!resource.exists()) {
            throw new IllegalStateException("선택한 Vendor Repository SQL이 없습니다. path=" + path);
        }
        try {
            String sql = resource.getContentAsString(StandardCharsets.UTF_8).trim();
            if (sql.isEmpty()) {
                throw new IllegalStateException("Vendor Repository SQL이 비어 있습니다. path=" + path);
            }
            return sql;
        } catch (IOException ex) {
            throw new IllegalStateException("Vendor Repository SQL을 읽을 수 없습니다. path=" + path, ex);
        }
    }

    private Path externalRelativePath(String statementKey) {
        return Path.of("runtime", moduleCode, "repository", statementKey + ".sql");
    }

    private static String requireSafeToken(String value, String fieldName) {
        if (value == null || !SAFE_TOKEN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다.");
        }
        return value.trim();
    }
}
