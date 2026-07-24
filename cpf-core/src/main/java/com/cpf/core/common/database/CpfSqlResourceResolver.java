package com.cpf.core.common.database;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
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
            Set.of("mariadb", "mysql", "postgresql", "oracle", "sqlserver");

    private CpfSqlResourceResolver() {
    }

    public static Resource[] mapperResources(Environment environment, String domainName) throws IOException {
        String vendor = normalizeVendor(environment);
        String domain = normalizeDomain(domainName);
        String pattern = "classpath*:mybatis/vendor/" + vendor + "/mapper/" + domain + "/**/*.xml";

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        Resource[] readable = Arrays.stream(resources)
                .filter(Resource::exists)
                .filter(Resource::isReadable)
                .toArray(Resource[]::new);

        if (readable.length == 0) {
            throw new IllegalStateException(
                    "CPF DB Vendor Runtime Mapper가 없습니다. vendor=" + vendor
                            + ", domain=" + domain + ", pattern=" + pattern);
        }
        return readable;
    }

    public static String repositoryResourceRoot(Environment environment, String domainName) {
        String vendor = normalizeVendor(environment);
        String domain = normalizeDomain(domainName);
        return "classpath:/sql/vendor/" + vendor + "/" + domain + "/";
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

    private static String normalizeDomain(String domainName) {
        if (domainName == null || !domainName.matches("^[a-z][a-z0-9-]{1,39}$")) {
            throw new IllegalArgumentException("유효하지 않은 CPF DomainName입니다: " + domainName);
        }
        return domainName;
    }
}
