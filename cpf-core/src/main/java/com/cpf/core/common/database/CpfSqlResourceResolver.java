package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CPF DB Vendor별 Runtime SQL/Mapper resource resolver.
 *
 * <p>Vendor가 다르면 SQL 문법도 다를 수 있으므로 다른 Vendor resource로 fallback하지 않는다.
 * 선택 Vendor의 resource가 없으면 기동을 실패시켜 잘못된 SQL이 운영에서 실행되는 것을 방지한다.</p>
 */
public final class CpfSqlResourceResolver {

    private static final Set<String> SUPPORTED =
            Set.of("mariadb", "postgresql", "oracle");

    private CpfSqlResourceResolver() {
    }

    /**
     * 선택 Vendor의 중앙 Mapper Pack 경로를 반환합니다.
     *
     * @param environment CPF Runtime 설정
     * @param domainName 중앙 Pack의 Runtime owner 이름
     * @return 선택 Pack의 재귀 XML filesystem pattern
     */
    public static String mapperPattern(Environment environment, String domainName) {
        String domain = normalizeDomain(domainName);
        Path packRoot = requiredPackRoot(environment);
        Path mapperRoot = CpfVendorResourceRoot.requiredDirectory(
                packRoot,
                Path.of("runtime", domain, "mybatis"),
                "Runtime Mapper");
        return filePattern(mapperRoot, "/**/*.xml");
    }

    public static Resource[] mapperResources(Environment environment, String domainName) throws IOException {
        String domain = normalizeDomain(domainName);
        Path packRoot = requiredPackRoot(environment);
        Path mapperRoot = CpfVendorResourceRoot.requiredDirectory(
                packRoot,
                Path.of("runtime", domain, "mybatis"),
                "Runtime Mapper");
        String pattern = filePattern(mapperRoot, "/**/*.xml");

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        List<ResolvedResource> readable = new ArrayList<>();
        for (Resource resource : resources) {
            if (!resource.exists() || !resource.isReadable()) {
                continue;
            }
            Path declaredPath = resource.getFile().toPath().toAbsolutePath().normalize();
            if (Files.isSymbolicLink(declaredPath)) {
                throw new IllegalStateException(
                        "Runtime Mapper symbolic link는 허용하지 않습니다. path=" + declaredPath);
            }
            Path realPath = declaredPath.toRealPath();
            if (!realPath.startsWith(packRoot)) {
                throw new IllegalStateException(
                        "Runtime Mapper symbolic link가 중앙 Vendor Pack을 벗어납니다. path="
                                + declaredPath);
            }
            if (!Files.isRegularFile(realPath, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            readable.add(new ResolvedResource(realPath, new FileSystemResource(realPath)));
        }

        if (readable.isEmpty()) {
            throw new IllegalStateException(
                    "선택된 중앙 CPF DB Vendor Runtime Mapper Pack이 비어 있습니다. vendor="
                            + normalizeVendor(environment)
                            + ", domain=" + domain + ", pattern=" + pattern);
        }
        readable.sort(Comparator.comparing(item -> item.path().toString()));
        return readable.stream()
                .map(ResolvedResource::resource)
                .toArray(Resource[]::new);
    }

    public static String repositoryResourceRoot(Environment environment, String domainName) {
        String domain = normalizeDomain(domainName);
        Path packRoot = requiredPackRoot(environment);
        Path repositoryRoot = CpfVendorResourceRoot.requiredDirectory(
                packRoot,
                Path.of("runtime", domain, "repository"),
                "Repository SQL");
        return filePattern(repositoryRoot, "/");
    }

    /**
     * 선택 Vendor 중앙 Pack의 Flyway filesystem location을 반환합니다.
     *
     * @param environment CPF Runtime 설정
     * @return {@code filesystem:<pack>/migration}
     */
    public static String flywayLocation(Environment environment) {
        Path packRoot = requiredPackRoot(environment);
        Path migrationRoot = CpfVendorResourceRoot.requiredDirectory(
                packRoot,
                Path.of("migration"),
                "Migration");
        return "filesystem:" + portablePath(migrationRoot);
    }

    public static String normalizeVendor(Environment environment) {
        String value = environment.getProperty("cpf.db.vendor");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("cpf.db.vendor 설정은 필수입니다.");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalStateException("지원하지 않는 CPF DB Vendor입니다: " + value);
        }
        return normalized;
    }

    private static Path requiredPackRoot(Environment environment) {
        CpfDatabaseVendor vendor = CpfDatabaseVendor.from(normalizeVendor(environment));
        return CpfVendorResourceRoot.required(environment, vendor);
    }

    private static String filePattern(Path root, String suffix) {
        return "file:" + portablePath(root) + suffix;
    }

    private static String portablePath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static String normalizeDomain(String domainName) {
        if (domainName == null || !domainName.matches("^[a-z][a-z0-9-]{1,39}$")) {
            throw new IllegalArgumentException("유효하지 않은 CPF DomainName입니다: " + domainName);
        }
        return domainName;
    }

    private record ResolvedResource(Path path, Resource resource) {
    }
}
