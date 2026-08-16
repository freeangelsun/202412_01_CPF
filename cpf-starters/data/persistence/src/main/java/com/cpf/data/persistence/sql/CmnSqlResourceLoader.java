package com.cpf.data.persistence.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Fail-closed classpath SQL query loader. Query text stays outside Java source. */
public final class CmnSqlResourceLoader {
    private static final String ROOT = "cpf-sql/cmn/";
    private static final Pattern SAFE = Pattern.compile("[a-z0-9][a-z0-9_./-]*\\.sql");
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private CmnSqlResourceLoader() { }
    public static String load(String queryId) {
        if (queryId == null || !SAFE.matcher(queryId).matches() || queryId.contains("..")) {
            throw new IllegalArgumentException("Invalid CMN queryId: " + queryId);
        }
        return CACHE.computeIfAbsent(queryId, CmnSqlResourceLoader::read);
    }
    private static String read(String queryId) {
        String path = ROOT + queryId;
        try (InputStream input = CmnSqlResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IllegalStateException("CMN SQL resource not found: " + path);
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (sql.isBlank()) throw new IllegalStateException("CMN SQL resource is blank: " + path);
            if (sql.contains("${")) throw new IllegalStateException("Unresolved CMN SQL token: " + path);
            return sql;
        } catch (IOException ex) {
            throw new IllegalStateException("CMN SQL resource read failed: " + path, ex);
        }
    }
}
