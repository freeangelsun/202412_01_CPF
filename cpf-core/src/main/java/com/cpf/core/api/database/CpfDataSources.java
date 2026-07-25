package com.cpf.core.api.database;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.core.env.Environment;

import javax.naming.NamingException;
import javax.sql.DataSource;

/** URL/JNDI DataSource 표준 설정을 해석하는 공개 facade입니다. */
public final class CpfDataSources {
    private CpfDataSources() { }
    public static DataSource resolve(Environment environment, String propertyPrefix) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, propertyPrefix);
    }
}
