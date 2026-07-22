package com.cpf.account.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;

import javax.sql.DataSource;

/** ACC MyBatis template이 지정된 factory를 사용하는지 검증합니다. */
class AccountMyBatisConfigTest {

    @Test
    void createsDedicatedSqlSessionTemplate() {
        SqlSessionFactory factory = mock(SqlSessionFactory.class);
        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment(
                "test",
                new JdbcTransactionFactory(),
                mock(DataSource.class)));
        when(factory.getConfiguration()).thenReturn(configuration);

        SqlSessionTemplate template = new AccountMyBatisConfig().accSqlSessionTemplate(factory);

        assertThat(template.getSqlSessionFactory()).isSameAs(factory);
    }
}
