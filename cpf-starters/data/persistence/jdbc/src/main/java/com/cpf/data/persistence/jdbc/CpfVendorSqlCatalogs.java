package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Verified Vendor Pack의 runtime/{module}/repository/*.sql을 fail-closed Catalog로 노출합니다. */
public final class CpfVendorSqlCatalogs {
    private CpfVendorSqlCatalogs() { }

    public static CpfVendorSqlCatalog fromPack(CpfDatabaseVendor vendor, String moduleCode, Path configuredPackRoot) {
        Objects.requireNonNull(vendor, "vendor");
        String module = require(moduleCode, "moduleCode").toLowerCase();
        Path root = Objects.requireNonNull(configuredPackRoot, "configuredPackRoot").toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve("pack.json"))) throw new IllegalArgumentException("Verified Vendor Pack requires pack.json: " + root);
        Path repository = root.resolve("runtime").resolve(module).resolve("repository").normalize();
        if (!repository.startsWith(root) || !Files.isDirectory(repository)) throw new IllegalArgumentException("Repository Query Pack not found: " + repository);
        Map<String, Path> statements = new HashMap<>();
        try (var stream = Files.list(repository)) {
            stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".sql")).forEach(path -> {
                String filename = path.getFileName().toString();
                String key = filename.substring(0, filename.length() - 4);
                if (statements.putIfAbsent(key, path) != null) throw new IllegalStateException("Duplicate SQL statement key: " + key);
            });
        } catch (IOException e) { throw new IllegalStateException("Failed to scan Query Pack: " + repository, e); }
        if (statements.isEmpty()) throw new IllegalArgumentException("No repository SQL found: " + repository);
        return new FileCatalog(vendor, Map.copyOf(statements));
    }

    private static String require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name); return value.trim(); }

    private record FileCatalog(CpfDatabaseVendor vendor, Map<String, Path> statements) implements CpfVendorSqlCatalog {
        @Override public String required(String statementKey) {
            Path path = requiredPath(statementKey);
            try {
                String sql = Files.readString(path, StandardCharsets.UTF_8).trim();
                if (sql.isEmpty()) throw new IllegalStateException("Empty SQL resource: " + path);
                return sql;
            } catch (IOException e) { throw new IllegalStateException("Failed to read SQL resource: " + path, e); }
        }
        @Override public String resourcePath(String statementKey) { return requiredPath(statementKey).toString(); }
        private Path requiredPath(String statementKey) {
            String key = require(statementKey, "statementKey");
            Path path = statements.get(key);
            if (path == null) throw new IllegalArgumentException("Unknown SQL statement key: " + key);
            return path;
        }
    }
}
