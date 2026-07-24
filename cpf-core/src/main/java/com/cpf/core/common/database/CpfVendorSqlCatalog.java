package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 중앙 DB Vendor Pack에서 Repository SQL을 읽는 fail-closed catalog입니다.
 *
 * <p>제품 Runtime의 물리 Vendor SQL 정본은
 * {@code {cpf.db.resource-root}/runtime/{module}/repository/{statement}.sql}입니다.
 * Module {@code src/main/resources}의 Vendor SQL fallback은 지원하지 않습니다. 따라서 중앙
 * Pack 선택 누락, 잘못된 Vendor, 누락된 Statement는 시작 또는 최초 접근 시 명시적으로 실패합니다.</p>
 *
 * <p>모든 Vendor Pack은 동일한 statement key와 parameter contract를 유지해야 하며
 * Controller/Application/Domain 코드는 Vendor를 분기하지 않습니다.</p>
 */
public final class CpfVendorSqlCatalog {
    private static final Pattern SAFE_TOKEN = Pattern.compile("[a-z][a-z0-9_-]{1,63}");

    private final CpfDatabaseVendor vendor;
    private final String moduleCode;
    private final Path externalResourceRoot;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private CpfVendorSqlCatalog(
            CpfDatabaseVendor vendor,
            String moduleCode,
            Path externalResourceRoot) {
        this.vendor = vendor;
        this.moduleCode = requireSafeToken(moduleCode, "moduleCode");
        this.externalResourceRoot = externalResourceRoot;
    }

    /**
     * Spring Environment의 Vendor 선택과 중앙 Pack root를 검증하여 Catalog를 생성합니다.
     *
     * @param environment CPF Runtime Environment
     * @param moduleCode 중앙 Pack의 논리 Owner code
     * @return fail-closed Repository SQL catalog
     * @throws IllegalStateException {@code cpf.db.resource-root}가 없거나 Pack 검증에 실패한 경우
     */
    public static CpfVendorSqlCatalog create(Environment environment, String moduleCode) {
        CpfDatabaseVendor vendor = CpfDatabaseVendor.from(
                environment.getProperty("cpf.db.vendor", "mariadb"));
        Path externalRoot = CpfVendorResourceRoot.required(environment, vendor);
        return new CpfVendorSqlCatalog(vendor, moduleCode, externalRoot);
    }

    /**
     * 지정 Statement SQL을 읽습니다. SQL은 UTF-8이며 빈 파일은 허용하지 않습니다.
     *
     * @param statementKey 중앙 Pack statement key
     * @return trim된 SQL text
     */
    public String required(String statementKey) {
        String safeKey = requireSafeToken(statementKey, "statementKey");
        return cache.computeIfAbsent(safeKey, this::readExternal);
    }

    /**
     * 중앙 Pack root 기준 상대 경로를 반환합니다.
     *
     * @param statementKey 중앙 Pack statement key
     * @return {@code runtime/<module>/repository/<statement>.sql}
     */
    public String resourcePath(String statementKey) {
        String safeKey = requireSafeToken(statementKey, "statementKey");
        return externalRelativePath(safeKey).toString().replace('\\', '/');
    }

    /**
     * 이 Catalog가 검증한 DB Vendor를 반환합니다.
     *
     * @return 선택된 CPF DB Vendor
     */
    public CpfDatabaseVendor vendor() {
        return vendor;
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
