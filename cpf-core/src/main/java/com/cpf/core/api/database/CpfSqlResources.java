package com.cpf.core.api.database;

import com.cpf.core.common.database.CpfSqlResourceResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;

/** Vendor별 Mapper/SQL resource를 선택하는 공개 facade입니다. */
public final class CpfSqlResources {
    private CpfSqlResources() { }
    public static Resource[] mapperResources(Environment environment, String domainName) throws IOException {
        return CpfSqlResourceResolver.mapperResources(environment, domainName);
    }
    public static String repositoryResourceRoot(Environment environment, String domainName) {
        return CpfSqlResourceResolver.repositoryResourceRoot(environment, domainName);
    }
    public static String vendor(Environment environment) {
        return CpfSqlResourceResolver.normalizeVendor(environment);
    }
}
