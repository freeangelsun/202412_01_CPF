package com.cpf.starter.data.persistence.mybatis;

import com.cpf.core.api.database.CpfDataOperations;
import com.cpf.core.api.database.CpfSqlResources;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Generated Domain mapper resource를 CPF 표준 DataSource와 연결합니다. */
@AutoConfiguration(afterName = "com.cpf.starter.persistence.jdbc.CpfDomainDataSourceAutoConfiguration")
@ConditionalOnProperty(prefix = "cpf.domain.persistence", name = "provider", havingValue = "mybatis")
@ConditionalOnBean(name = {"cpfDomainDataSource", "cpfDomainTransactionManager"})
public class CpfDomainMyBatisAutoConfiguration {
    @Bean("cpfDomainSqlSessionFactory")
    @ConditionalOnMissingBean(name = "cpfDomainSqlSessionFactory")
    SqlSessionFactory cpfDomainSqlSessionFactory(
            @Qualifier("cpfDomainDataSource") DataSource dataSource, Environment environment) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(CpfSqlResources.mapperResources(environment, "*"));
        return bean.getObject();
    }
    @Bean("cpfDomainSqlSessionTemplate")
    @ConditionalOnMissingBean(name = "cpfDomainSqlSessionTemplate")
    SqlSessionTemplate cpfDomainSqlSessionTemplate(
            @Qualifier("cpfDomainSqlSessionFactory") SqlSessionFactory factory) {
        return new SqlSessionTemplate(factory);
    }
    @Bean
    @ConditionalOnMissingBean(CpfDataOperations.class)
    CpfDataOperations cpfDataOperations(
            @Qualifier("cpfDomainSqlSessionTemplate") SqlSessionTemplate session,
            @Qualifier("cpfDomainTransactionManager") PlatformTransactionManager transactionManager) {
        return new CpfMyBatisDataOperations(session, new TransactionTemplate(transactionManager));
    }
}
