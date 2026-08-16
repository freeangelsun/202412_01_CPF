package com.cpf.data.persistence.jdbc;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.core.env.Environment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Objects;

/** JDBC Provider 내부 DataSource resolver. Core/Foundation에 Spring/JNDI 구현을 노출하지 않습니다. */
public final class CpfDataSources {
    private CpfDataSources() { }

    public static DataSource resolve(Environment environment, String propertyPrefix) throws NamingException {
        Objects.requireNonNull(environment, "environment");
        String prefix = require(propertyPrefix, "propertyPrefix");
        String jndiName = trim(environment.getProperty(prefix + ".jndi-name"));
        if (jndiName != null) {
            Object value = new InitialContext().lookup(jndiName);
            if (!(value instanceof DataSource ds)) throw new NamingException("JNDI object is not a DataSource: " + jndiName);
            return ds;
        }
        String url = trim(environment.getProperty(prefix + ".url"));
        if (url == null) throw new IllegalStateException(prefix + ".url or " + prefix + ".jndi-name is required");
        String driver = trim(environment.getProperty(prefix + ".driver-class-name"));
        if (driver != null) {
            try { Class.forName(driver); }
            catch (ClassNotFoundException e) { throw new IllegalStateException("JDBC driver not found: " + driver, e); }
        }
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        String username = trim(environment.getProperty(prefix + ".username"));
        if (username != null) dataSource.setUsername(username);
        String password = environment.getProperty(prefix + ".password");
        if (password != null) dataSource.setPassword(password);
        if (driver != null) dataSource.setDriverClassName(driver);
        String poolName = trim(environment.getProperty(prefix + ".pool-name"));
        if (poolName != null) dataSource.setPoolName(poolName);
        return dataSource;
    }

    private static String require(String value, String name) {
        String normalized = trim(value); if (normalized == null) throw new IllegalArgumentException(name); return normalized;
    }
    private static String trim(String value) { if (value == null) return null; String v=value.trim(); return v.isEmpty()?null:v; }
}
