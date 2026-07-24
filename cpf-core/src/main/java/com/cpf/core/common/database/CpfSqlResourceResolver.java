package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 선택된 DB Vendor의 Mapper SQL resource pack만 로딩합니다.
 *
 * <p>Vendor 전환은 이 인프라 경계와 SQL 파일에서 처리하며 업무 Java Source를 바꾸지 않습니다.
 * 중앙 외부 pack이 설정되면 다른 Vendor 또는 classpath SQL로 fallback하지 않습니다.
 * 외부 pack을 설정하지 않은 호환 모드에서만 선택 Vendor의 classpath overlay를 사용합니다.</p>
 */
public final class CpfSqlResourceResolver {
    private static final Pattern MODULE_CODE = Pattern.compile("[a-z][a-z0-9_-]{1,63}");

    private CpfSqlResourceResolver() {
    }

    public static String mapperPattern(Environment environment, String moduleCode) {
        String module = requireModuleCode(moduleCode);
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Optional<Path> externalRoot = CpfVendorResourceRoot.selected(environment, vendor);
        if (externalRoot.isPresent()) {
            Path relativePath = Path.of("runtime", module, "mybatis");
            Path mapperRoot = CpfVendorResourceRoot.requiredDirectory(
                    externalRoot.get(),
                    relativePath,
                    "MyBatis");
            return "file:" + mapperRoot.toString().replace('\\', '/') + "/**/*.xml";
        }
        return "classpath*:mybatis/vendor/%s/mapper/%s/**/*.xml"
                .formatted(vendor.id(), module);
    }

    public static Resource[] mapperResources(Environment environment, String moduleCode)
            throws IOException {
        String module = requireModuleCode(moduleCode);
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Optional<Path> externalRoot = CpfVendorResourceRoot.selected(environment, vendor);
        if (externalRoot.isPresent()) {
            return externalMapperResources(externalRoot.get(), module);
        }
        String pattern = mapperPattern(environment, moduleCode);
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        if (resources.length == 0) {
            throw new IllegalStateException(
                    "선택한 DB Vendor의 Mapper SQL resource pack이 없습니다. pattern=" + pattern);
        }
        return resources;
    }

    public static String flywayLocation(Environment environment) {
        CpfDatabaseVendor vendor = selectedVendor(environment);
        Optional<Path> externalRoot = CpfVendorResourceRoot.selected(environment, vendor);
        if (externalRoot.isPresent()) {
            Path migrationRoot = CpfVendorResourceRoot.requiredDirectory(
                    externalRoot.get(),
                    Path.of("migration"),
                    "Migration");
            return "filesystem:" + migrationRoot.toString().replace('\\', '/');
        }
        return vendor.flywayLocation();
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
