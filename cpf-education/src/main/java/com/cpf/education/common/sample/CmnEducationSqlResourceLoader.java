package com.cpf.education.common.sample;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Education/EDU CMN Sample의 classpath SQL loader입니다.
 *
 * <p>DML Query 예제는 cpf-education가 소비자 관점에서 소유하지만, Schema/Migration/Seed 생명주기는 DB Tool의 REFERENCE_FIXTURE가 소유합니다.</p>
 */
final class CmnEducationSqlResourceLoader {
    private static final String ROOT = "cpf-sql/cmn/";
    private static final Pattern SAFE = Pattern.compile("[a-z0-9][a-z0-9_./-]*\\.sql");
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private CmnEducationSqlResourceLoader() {
    }

    static String load(String queryId) {
        if (queryId == null || !SAFE.matcher(queryId).matches() || queryId.contains("..")) {
            throw new IllegalArgumentException("Invalid Education CMN queryId: " + queryId);
        }
        return CACHE.computeIfAbsent(queryId, CmnEducationSqlResourceLoader::read);
    }

    private static String read(String queryId) {
        String path = ROOT + queryId;
        try (InputStream input = CmnEducationSqlResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Education CMN SQL resource not found: " + path);
            }
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (sql.isBlank()) {
                throw new IllegalStateException("Education CMN SQL resource is blank: " + path);
            }
            if (sql.contains("${")) {
                throw new IllegalStateException("Unresolved Education CMN SQL token: " + path);
            }
            return sql;
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IOException ex) {
            throw new IllegalStateException("Education CMN SQL resource read failed: " + path, ex);
        }
    }
}
