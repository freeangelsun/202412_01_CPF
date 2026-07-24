package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Optional;

/**
 * 선택된 중앙 DB Vendor Pack의 외부 filesystem root를 검증합니다.
 *
 * <p>{@code cpf.db.resource-root}가 설정되면 이 root만 사용합니다. 모든 하위 resource는
 * 정규화된 실제 경로가 root 안에 있는지 다시 확인하므로 경로 순회와 symbolic link를
 * 통한 pack 외부 탈출을 허용하지 않습니다.</p>
 */
final class CpfVendorResourceRoot {
    static final String PROPERTY_NAME = "cpf.db.resource-root";
    private static final long MAX_MANIFEST_BYTES = 65_536;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CpfVendorResourceRoot() {
    }

    static Optional<Path> selected(
            Environment environment,
            CpfDatabaseVendor selectedVendor) {
        String configured = environment.getProperty(PROPERTY_NAME);
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }
        if (!configured.equals(configured.trim())) {
            throw new IllegalArgumentException(PROPERTY_NAME + " 앞뒤에 공백을 사용할 수 없습니다.");
        }

        final Path configuredPath;
        try {
            configuredPath = Path.of(configured).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException(PROPERTY_NAME + " 경로가 올바르지 않습니다.", ex);
        }

        try {
            Path realRoot = configuredPath.toRealPath();
            if (!Files.isDirectory(realRoot, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException(
                        "선택한 DB Vendor resource root가 디렉터리가 아닙니다. root=" + configuredPath);
            }
            validateManifest(realRoot, selectedVendor);
            return Optional.of(realRoot);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "선택한 DB Vendor resource root를 확인할 수 없습니다. root=" + configuredPath,
                    ex);
        }
    }

    private static void validateManifest(Path realRoot, CpfDatabaseVendor selectedVendor) {
        Path manifest = requiredFile(realRoot, Path.of("pack.json"), "Pack Manifest");
        try {
            if (Files.size(manifest) > MAX_MANIFEST_BYTES) {
                throw new IllegalStateException(
                        "DB Vendor pack.json 크기가 제한을 초과합니다. path=" + manifest);
            }
            JsonNode root = OBJECT_MAPPER.readTree(manifest.toFile());
            if (root == null || !root.isObject()) {
                throw new IllegalStateException(
                        "DB Vendor pack.json 최상위 값은 object여야 합니다. path=" + manifest);
            }
            JsonNode vendorNode = root.get("vendor");
            JsonNode schemaVersionNode = root.get("schemaVersion");
            JsonNode statusNode = root.get("status");
            if (vendorNode == null || !vendorNode.isTextual()
                    || schemaVersionNode == null || !schemaVersionNode.canConvertToInt()
                    || statusNode == null || !statusNode.isTextual() || statusNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "DB Vendor pack.json 필수 필드가 올바르지 않습니다. "
                                + "required=vendor,schemaVersion,status path=" + manifest);
            }
            CpfDatabaseVendor manifestVendor = CpfDatabaseVendor.from(vendorNode.asText());
            if (manifestVendor != selectedVendor) {
                throw new IllegalStateException(
                        "cpf.db.vendor와 DB Vendor pack.json vendor가 일치하지 않습니다. "
                                + "selected=" + selectedVendor.id()
                                + ", pack=" + manifestVendor.id());
            }
            if (schemaVersionNode.intValue() != 1) {
                throw new IllegalStateException(
                        "지원하지 않는 DB Vendor pack schemaVersion입니다. expected=1, actual="
                                + schemaVersionNode.intValue());
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "DB Vendor pack.json을 검증할 수 없습니다. path=" + manifest,
                    ex);
        }
    }

    static Path requiredDirectory(Path realRoot, Path relativePath, String resourceKind) {
        Path realPath = requiredRealPath(realRoot, relativePath, resourceKind);
        if (!Files.isDirectory(realPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException(
                    "선택한 DB Vendor " + resourceKind + " 경로가 디렉터리가 아닙니다. path="
                            + display(realRoot, relativePath));
        }
        return realPath;
    }

    static Path requiredFile(Path realRoot, Path relativePath, String resourceKind) {
        Path realPath = requiredRealPath(realRoot, relativePath, resourceKind);
        if (!Files.isRegularFile(realPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException(
                    "선택한 DB Vendor " + resourceKind + " 파일이 아닙니다. path="
                            + display(realRoot, relativePath));
        }
        return realPath;
    }

    private static Path requiredRealPath(Path realRoot, Path relativePath, String resourceKind) {
        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException(resourceKind + " 상대 경로만 사용할 수 있습니다.");
        }

        Path candidate = realRoot.resolve(relativePath).normalize();
        if (!candidate.startsWith(realRoot)) {
            throw new IllegalStateException(
                    "선택한 DB Vendor " + resourceKind + " 경로가 resource root를 벗어납니다. path="
                            + display(realRoot, relativePath));
        }

        final Path realPath;
        try {
            realPath = candidate.toRealPath();
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "선택한 DB Vendor " + resourceKind + " resource가 없습니다. path="
                            + display(realRoot, relativePath),
                    ex);
        }
        if (!realPath.startsWith(realRoot)) {
            throw new IllegalStateException(
                    "선택한 DB Vendor " + resourceKind
                            + " symbolic link가 resource root를 벗어납니다. path="
                            + display(realRoot, relativePath));
        }
        return realPath;
    }

    private static Path display(Path realRoot, Path relativePath) {
        return realRoot.resolve(relativePath).normalize();
    }
}
