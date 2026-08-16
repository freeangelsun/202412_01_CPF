package com.cpf.data.persistence.mybatis;

import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** 공식 DB Vendor Pack에서 MyBatis Mapper resource를 결정적으로 선택하는 Provider 내부 resolver입니다. */
public final class CpfSqlResources {
    private CpfSqlResources() { }

    public static Resource[] mapperResources(Environment environment, String domainName) throws IOException {
        Path root = resourceRoot(environment);
        String domain = domainName == null || domainName.isBlank() ? "*" : domainName.trim().toLowerCase();
        Path runtime = root.resolve("runtime");
        if (!Files.isDirectory(runtime)) throw new IOException("CPF DB runtime root not found: " + runtime);
        List<Path> files = new ArrayList<>();
        try (var stream = Files.walk(runtime)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .filter(path -> path.toString().replace('\\','/').contains("/mybatis/"))
                    .filter(path -> "*".equals(domain) || belongsToDomain(runtime, path, domain))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(files::add);
        }
        if (files.isEmpty()) throw new IOException("No MyBatis mapper resources for domain=" + domain + " under " + runtime);
        return files.stream().map(FileSystemResource::new).toArray(Resource[]::new);
    }

    public static String repositoryResourceRoot(Environment environment, String domainName) {
        String domain = Objects.requireNonNull(domainName, "domainName").trim().toLowerCase();
        return resourceRoot(environment).resolve("runtime").resolve(domain).resolve("repository").toString();
    }

    public static String vendor(Environment environment) {
        String vendor = environment.getProperty("cpf.db.vendor", "mariadb").trim().toLowerCase();
        if (!vendor.equals("mariadb") && !vendor.equals("postgresql") && !vendor.equals("oracle"))
            throw new IllegalArgumentException("Unsupported CPF DB vendor: " + vendor);
        return vendor;
    }

    private static Path resourceRoot(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        String configured = environment.getProperty("cpf.db.resource-root");
        if (configured == null || configured.isBlank()) throw new IllegalStateException("cpf.db.resource-root is required");
        Path root = Path.of(configured.trim()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) throw new IllegalStateException("cpf.db.resource-root not found: " + root);
        return root;
    }

    private static boolean belongsToDomain(Path runtime, Path path, String domain) {
        Path relative = runtime.relativize(path);
        return relative.getNameCount() > 0 && relative.getName(0).toString().equalsIgnoreCase(domain);
    }
}
