package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 선택된 DB Vendor의 Mapper SQL resource pack만 로딩합니다.
 *
 * <p>Vendor 전환은 이 인프라 경계와 SQL 파일에서 처리하며 업무 Java Source를 바꾸지 않습니다.
 * 중앙 Vendor Pack이 설정되지 않았거나 필요한 resource가 없으면 fail-fast 합니다.
 * Module-local classpath Vendor SQL fallback은 제품 Runtime에서 허용하지 않습니다.</p>
 */
public final class CpfSqlResourceResolver {
    private static final Pattern MODULE_CODE = Pattern.compile("[a-z][a-z0-9_-]{1,63}");

    private CpfSqlResourceResolver() {
    }

    /**
     * 선택 Vendor Pack의 MyBatis XML 검색 Pattern을 반환합니다.
     *
     * @param environment CPF Runtime Environment
     * @param moduleCode 중앙 Pack의 논리 SQL Owner code
     * @return {@code file:<resourceRoot>/runtime/<module>/mybatis/**/*.xml} Pattern
     * @throws IllegalStateException 중앙 Pack 또는 MyBatis Directory가 없거나 Pack 검증에 실패한 경우
     */
    public static String mapperPattern(Environment environment, String moduleCode) {
        String module = requireModuleCode(moduleCode);
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Path externalRoot = CpfVendorResourceRoot.required(environment, vendor);
        Path relativePath = Path.of("runtime", module, "mybatis");
        Path mapperRoot = CpfVendorResourceRoot.requiredDirectory(
                externalRoot,
                relativePath,
                "MyBatis");
        return "file:" + mapperRoot.toString().replace('\\', '/') + "/**/*.xml";
    }

    /**
     * 선택 Vendor Pack에서 실제 MyBatis XML Resource 목록을 결정적으로 로딩합니다.
     *
     * @param environment CPF Runtime Environment
     * @param moduleCode 중앙 Pack의 논리 SQL Owner code
     * @return 설명 문자열 기준으로 정렬된 MyBatis XML Resource
     * @throws IOException filesystem resource 순회에 실패한 경우
     * @throws IllegalStateException 중앙 Pack이 없거나 선택 Owner의 Mapper가 비어 있는 경우
     */
    public static Resource[] mapperResources(Environment environment, String moduleCode)
            throws IOException {
        String module = requireModuleCode(moduleCode);
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Path externalRoot = CpfVendorResourceRoot.required(environment, vendor);
        return externalMapperResources(externalRoot, module);
    }

    /**
     * 선택 Vendor Pack의 Flyway migration filesystem location을 반환합니다.
     *
     * @param environment CPF Runtime Environment
     * @return {@code filesystem:<resourceRoot>/migration} 형식의 Flyway location
     * @throws IllegalStateException 중앙 Pack 또는 migration Directory가 없는 경우
     */
    public static String flywayLocation(Environment environment) {
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Path externalRoot = CpfVendorResourceRoot.required(environment, vendor);
        Path migrationRoot = CpfVendorResourceRoot.requiredDirectory(
                externalRoot,
                Path.of("migration"),
                "Migration");
        return "filesystem:" + migrationRoot.toString().replace('\\', '/');
    }

    private static Resource[] externalMapperResources(Path externalRoot, String moduleCode)
            throws IOException {
        Path mapperRoot = CpfVendorResourceRoot.requiredDirectory(
                externalRoot,
                Path.of("runtime", moduleCode, "mybatis"),
                "MyBatis");
        List<Resource> resources;
        try (Stream<Path> paths = Files.walk(mapperRoot)) {
            resources = paths
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .map(path -> externalMapperResource(externalRoot, path))
                    .sorted(Comparator.comparing(Resource::getDescription))
                    .toList();
        }
        if (resources.isEmpty()) {
            throw new IllegalStateException(
                    "선택한 DB Vendor의 외부 MyBatis resource pack이 비어 있습니다. path="
                            + mapperRoot);
        }
        return resources.toArray(Resource[]::new);
    }

    private static Resource externalMapperResource(Path externalRoot, Path candidate) {
        Path relativePath = externalRoot.relativize(candidate.toAbsolutePath().normalize());
        Path realResource = CpfVendorResourceRoot.requiredFile(
                externalRoot,
                relativePath,
                "MyBatis");
        return new FileSystemResource(realResource);
    }

    private static CpfDatabaseVendor selectedVendor(Environment environment) {
        return CpfDatabaseVendor.from(environment.getProperty("cpf.db.vendor", "mariadb"));
    }

    private static String requireModuleCode(String moduleCode) {
        if (moduleCode == null || !MODULE_CODE.matcher(moduleCode.trim()).matches()) {
            throw new IllegalArgumentException(
                    "moduleCode는 소문자로 시작하는 영숫자/밑줄/하이픈 2~64자리여야 합니다.");
        }
        return moduleCode.trim();
    }
}
