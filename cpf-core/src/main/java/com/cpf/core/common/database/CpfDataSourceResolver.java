package com.cpf.core.common.database;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * URL 직접 연결과 외부 WAS JNDI 연결을 동일한 설정 계약으로 해석합니다.
 *
 * <p>각 모듈은 자신이 소유한 설정 접두어만 전달합니다. 비밀번호를 로그에 남기지 않으며,
 * 지원하지 않는 모드는 기동 시 즉시 실패시켜 잘못된 배포 설정이 운영 요청까지 지연되지 않게 합니다.</p>
 */
public final class CpfDataSourceResolver {

    private CpfDataSourceResolver() {
    }

    /**
     * 기본 JNDI 접근 객체를 사용해 DataSource를 생성하거나 조회합니다.
     *
     * @param environment Spring 환경 설정
     * @param propertyPrefix DataSource 설정 접두어
     * @return URL 또는 JNDI 방식으로 확인한 DataSource
     * @throws NamingException JNDI 조회 실패 시 발생
     */
    public static DataSource resolve(Environment environment, String propertyPrefix) throws NamingException {
        return resolve(environment, propertyPrefix, new JndiTemplate());
    }

    /**
     * 테스트 가능한 JNDI 접근 객체를 받아 DataSource를 생성하거나 조회합니다.
     *
     * @param environment Spring 환경 설정
     * @param propertyPrefix DataSource 설정 접두어
     * @param jndiTemplate JNDI 조회 객체
     * @return URL 또는 JNDI 방식으로 확인한 DataSource
     * @throws NamingException JNDI 조회 실패 시 발생
     */
    public static DataSource resolve(
            Environment environment,
            String propertyPrefix,
            JndiTemplate jndiTemplate) throws NamingException {
        String prefix = requireText(propertyPrefix, "propertyPrefix");
        String mode = environment.getProperty(prefix + ".mode", "url");
        if ("jndi".equalsIgnoreCase(mode)) {
            String jndiName = environment.getRequiredProperty(prefix + ".jndi-name");
            return jndiTemplate.lookup(jndiName, DataSource.class);
        }
        if (!"url".equalsIgnoreCase(mode)) {
            throw new IllegalArgumentException(prefix + ".mode는 url 또는 jndi여야 합니다.");
        }
        return DataSourceBuilder.create()
                .url(environment.getRequiredProperty(prefix + ".url"))
                .username(environment.getRequiredProperty(prefix + ".username"))
                .password(environment.getRequiredProperty(prefix + ".password"))
                .driverClassName(environment.getProperty(
                        prefix + ".driver-class-name",
                        "org.mariadb.jdbc.Driver"))
                .build();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value.trim();
    }
}
