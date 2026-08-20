package com.cpf.common.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Common Product가 소유한 fail-closed classpath SQL loader입니다. */
public final class CpfCommonSqlResourceLoader {
    private static final String ROOT = "cpf-sql/cmn/";
    private static final Pattern SAFE = Pattern.compile("[a-z0-9][a-z0-9_./-]*\\.sql");
    private static final Map<String,String> CACHE = new ConcurrentHashMap<>();
    private CpfCommonSqlResourceLoader() { }
    public static String load(String queryId) {
        if (queryId == null || !SAFE.matcher(queryId).matches() || queryId.contains("..")) {
            throw new IllegalArgumentException("Invalid CMN queryId: " + queryId);
        }
        return CACHE.computeIfAbsent(queryId, CpfCommonSqlResourceLoader::read);
    }
    private static String read(String queryId) {
        String path = ROOT + queryId;
        try (InputStream input = CpfCommonSqlResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
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
